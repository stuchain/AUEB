package ds;

import java.util.ArrayList;

public class Results implements Comparable<Results>
{
    private ArrayList<RoomArray> results;   // array with selected rooms based on given filter
    private int id; 

    Results()
    {
        results = new ArrayList<>();
    }
    // Getters
    public int getId() {
        return id;
    }
    public ArrayList<RoomArray> getResults() {
        return results;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setResults(ArrayList<RoomArray> results) {
        this.results = results;
    }

    // Prints request id and rooms of results arraylist
    public void printRooms() throws InterruptedException
    {
        System.out.println();
        System.out.println();
        System.out.println("+-------------- Request: " +  this.id + " --------------+");
        for(RoomArray result : results) {
            result.printRooms();                       // prints each room's data
            System.out.println();
        }
    }
    // Compares reducer objects based on their current ID
    @Override
    public int compareTo(Results other)
    {
        return Integer.compare(this.id, other.id);
    }
}