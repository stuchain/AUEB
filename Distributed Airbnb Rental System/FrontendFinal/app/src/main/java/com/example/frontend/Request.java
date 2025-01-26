package com.example.frontend;
public class Request                // This class is getting extended by Filter,Review and Room
{
    private int id;                 // id of request
    private int managerID;          // instance for manager ID

    // Setters
    public void setId(int id) {     
        this.id = id;
    }
    public void setManagerID(int managerID) {
        this.managerID = managerID;
    }
    // Getters
    public int getId() 
    {            
        return id;
    }

    public int getManagerID() {
        return managerID;
    }
}