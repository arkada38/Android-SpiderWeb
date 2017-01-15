package ru.arkada38.SpiderWeb;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.BaseGameUtils;

import static ru.arkada38.SpiderWeb.Settings.NUMBER_OF_LVL;
import static ru.arkada38.SpiderWeb.Settings.TAG;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    GridView mainGridView;
    LvlAdapter lvlAdapter;
    Intent intent;
    private static Context context;

    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;
    private static GoogleApiClient mGoogleApiClient;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;
    private boolean mAutoStartSignInFlow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        context = this;
        Settings.sPref = getPreferences(Context.MODE_PRIVATE);

        // Create the Google Api Client with access to Games
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        LvlKeeper.initLvls();

        mainGridView = (GridView) findViewById(R.id.gridView);
        lvlAdapter = new LvlAdapter(this, LvlKeeper.getLvls());
        mainGridView.setAdapter(lvlAdapter);
        intent = new Intent(this, LvlActivity.class);

        mainGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Можно проходить пройденные уровни и ближайщий непройденный уровень
                if (i <= Settings.getMaxLvl() + 1) {
                    intent.putExtra(NUMBER_OF_LVL, i);
                    startActivity(intent);
                }
                else
                    Toast.makeText(context, R.string.on_enter_to_close_lvl, Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onResume() {
        // При возвращении на главную страницу после прохождения уровней,
        // игрок должен увидеть изменения
        lvlAdapter.notifyDataSetChanged();
        super.onResume();
    }

    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.leaderboard:
                Toast.makeText(this, "leaderboard", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.achievements:
                Toast.makeText(this, "achievements", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.about:
                Toast.makeText(this, "about", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region Google Play
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
        if (isSignedIn()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected() called. Sign in successful!");
        submitLvl();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended() called. Trying to reconnect.");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed() called, result: " + connectionResult);

        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    connectionResult, RC_SIGN_IN, "Error");
        }
    }

    private boolean isSignedIn() {
        return (mGoogleApiClient != null && mGoogleApiClient.isConnected());
    }

    private static boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult) {
        return scoreResult != null && GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }

    public static void submitLvl() {
        if (mGoogleApiClient.isConnected()) {
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, context.getString(R.string.leaderboard_max_lvl), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                    .setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                        @Override
                        public void onResult(Leaderboards.LoadPlayerScoreResult scoreResult) {
                            if (isScoreResultValid(scoreResult)) {
                                Long c = scoreResult.getScore().getRawScore();
                                int MaxLvl = Settings.getMaxLvl();

                                if (c >= MaxLvl && c <= LvlKeeper.getNumberOfLvls())
                                    Settings.setMaxLvl((int) (long) c);
                                else
                                    Games.Leaderboards.submitScore(mGoogleApiClient, context.getString(R.string.leaderboard_max_lvl), MaxLvl);

                                if (MaxLvl >= 3)
                                    Games.Achievements.unlock(mGoogleApiClient, context.getString(R.string.achievement_origin));
                                if (MaxLvl >= 15)
                                    Games.Achievements.unlock(mGoogleApiClient, context.getString(R.string.achievement_get_ones_hand_in));
                                if (MaxLvl >= 30)
                                    Games.Achievements.unlock(mGoogleApiClient, context.getString(R.string.achievement_experienced));
                                if (MaxLvl >= 60)
                                    Games.Achievements.unlock(mGoogleApiClient, context.getString(R.string.achievement_master));
                                if (MaxLvl == LvlKeeper.getNumberOfLvls())
                                    Games.Achievements.unlock(mGoogleApiClient, context.getString(R.string.achievement_the_game_passed));
                            } else {
                                Games.Leaderboards.submitScore(mGoogleApiClient, context.getString(R.string.leaderboard_max_lvl), 0);
                                submitLvl();
                            }
                        }
                    });
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, context.getString(R.string.leaderboard_steps), LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                    .setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                        @Override
                        public void onResult(Leaderboards.LoadPlayerScoreResult scoreResult) {
                            if (isScoreResultValid(scoreResult)) {
                                Long c = scoreResult.getScore().getRawScore();
                                Games.Leaderboards.submitScore(mGoogleApiClient, context.getString(R.string.leaderboard_steps), c + 1);
                            } else {
                                Games.Leaderboards.submitScore(mGoogleApiClient, context.getString(R.string.leaderboard_steps), 1);
                            }
                        }
                    });
        }
    }
    //endregion
}