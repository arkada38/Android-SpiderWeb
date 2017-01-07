package ru.arkada38.SpiderWeb;

public class LvlItem {

    private Integer numberOfSpiders;
    private Integer numbersOfConnections;

    private String coordinatesString;
    private String contactsString;

    public LvlItem(String coordinatesString, String contactsString) {
        this.coordinatesString = coordinatesString;
        this.contactsString = contactsString;
        this.numberOfSpiders = getCoordinates().length / 2;
        this.numbersOfConnections = getContacts().length / 2;
    }

    public Integer getNumberOfSpiders() {
        return numberOfSpiders;
    }

    public Integer getNumbersOfConnections() {
        return numbersOfConnections;
    }

    public int[] getCoordinates() {
        String[] coordinatesStringArray = coordinatesString.split(" ");
        int[] coordinates = new int[coordinatesStringArray.length];

        for (int i = 0; i < coordinatesStringArray.length; i++)
            coordinates[i] = Integer.parseInt(coordinatesStringArray[i]);

        return coordinates;
    }

    public int[] getContacts() {
        String[] contactsStringArray = contactsString.split(" ");
        int[] contacts = new int[contactsStringArray.length];

        for (int i = 0; i < contactsStringArray.length; i++)
            contacts[i] = Integer.parseInt(contactsStringArray[i]);

        return contacts;
    }
}
