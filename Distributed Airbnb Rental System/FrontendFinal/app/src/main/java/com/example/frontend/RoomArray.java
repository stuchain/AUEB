package com.example.frontend;

import java.util.ArrayList;

public class RoomArray 
{
    private ArrayList<Room> rooms;                   // array with searching results 
    private int id;                                  // id of array 

    // Costructor 
    RoomArray()
    {
        this.rooms = new ArrayList<>();            // initialize a new array when the method is called   
    }
    // Getters
    public int getId() {
        return id;                                
    }
    public ArrayList<Room> getRooms() {
        return rooms;
    }
    // Setter
    public void setId(int id) {
        this.id = id;
    }
    // Adds room to array
    public  void addRoom(Room room)
    {
        rooms.add(room);
    }
    // Prints rooms of rooms array
    public void printRooms()
    {
        for(Room room : rooms) {
            System.out.println(room.toString());
            System.out.println();
        }
    }
}