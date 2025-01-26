package com.example.frontend;

import java.lang.reflect.Field;
import java.util.ArrayList;

// Room Class
public class Room extends Request
{
    private String roomName;                            // room of the name
    private int guests;                                 // number of guests
    private double price;                               // price
    private double stars;                               // average valye of stars
    private String area;                                // area
    private int reviews;                                // number of reviews 
    private String roomImage;                           // room image ( will be shown in part b )
    private String startDate;                           // starting date of eligible booking
    private String endDate;                             // ending date of eligible booking
    private boolean available;                          // flag of availability
    private DateRange dateRange;                        // daterange
    private ArrayList<DateRange> bookings;              // array with date ranges of room's bookings
    
    // Constructor for when starting date and ending date are given 
    Room(int id, String name, int guests, double price, double stars,
            String area, int reviews, String image, String start, String end, boolean available, ArrayList<DateRange> bookings)
    {
        this.setManagerID(id);      
        this.roomName = name;
        this.guests = guests;
        this.price = price;
        this.stars = stars;
        this.area = area;
        this.reviews = reviews;
        this.roomImage = image;
        this.startDate = start;
        this.endDate = end;
        this.available = available;
        this.dateRange = new DateRange();
        this.dateRange.setStartDate(this.startDate);
        this.dateRange.setEndDate(this.endDate);
        this.bookings = bookings;
    }
    // Constructor for null values of start & end dates
    // because of LocalDate parse() method
    Room(int id, String name, int guests, double price, double stars,
        String area, int reviews, String image, boolean available)
    {
        this.setManagerID(id);
        this.roomName = name;
        this.guests = guests;
        this.price = price;
        this.stars = stars;
        this.area = area;
        this.reviews = reviews;
        this.roomImage = image;
        this.available = available;
    }
    // Default constructor
    Room()
    {
    }
    // Getters
    public DateRange getDateRange() {
        return dateRange;
    }
    public String getRoomName() {
        return roomName;
    }
    public int getGuests() {
        return guests;
    }
    public double getPrice() {
        return price;
    }
    public double getStars() {
        return stars;
    }
    public String getArea() {
        return area;
    }
    public int getReviews() {
        return reviews;
    }
    public String getRoomImage() {
        return roomImage;
    }
    public String getEndDate() {
        return endDate;
    }
    public String getStartDate() {
        return startDate;
    }
    public boolean getAvailable()
    {
        return available;
    }
    public ArrayList<DateRange> getBookings() {
        return bookings;
    }
    
    // Setters
    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }
    public void setGuests(int guestNum) {
        this.guests = guestNum;
    }
    public void setPrice(double price) {
        this.price = price;
    }
    public void setStars(double stars) {
        this.stars = stars;
    }
    public void setArea(String area) {
        this.area = area;
    }
    public void setReviews(int reviews) {
        this.reviews = reviews;
    }
    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }
    public void setDateRange(String startDate, String endDate) {
        this.dateRange = new DateRange();
        this.startDate = startDate;
        this.endDate = endDate;
        this.dateRange.setStartDate(startDate);
        this.dateRange.setEndDate(endDate);
    }
    public void setBookings(ArrayList<DateRange> bookings) {
        this.bookings = bookings;
    }
    // Adds date range of booking to bookings array
    public synchronized void addBooking(DateRange bookingDateRange)
    {
        this.bookings.add(bookingDateRange);
    }
    // Computes how many non null or non 0 values does this object has
    public int numNonZero()
    {
        int count = 0;
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if(value != null && value.getClass() == DateRange.class) {                      // if value of field belongs to data range class
                    DateRange dateRange = (DateRange) value;                                    // cast value to data range object
                    if(dateRange.getStartDate() == null || dateRange.getEndDate() == null) {    // if start date or end date is null  
                        continue;
                    } else {
                        count++;                                                                // increase count  
                    }
                } else if(value != null && !value.equals(0) && !value.equals(0.0)) {            // if value is not null/0/0.0 
                    count++;                                                                    // increase count
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    // Copies this object to another
    public Room copy()
    {
        if(this.startDate == null || this.endDate == null) {
            return new Room(this.getManagerID(), this.roomName, this.guests, this.price,                            // for rooms where dates are not given yet
            this.stars, this.area, this.reviews, this.roomImage, this.available);
        } else {
            return new Room(this.getManagerID(), this.roomName, this.guests, this.price, 
            this.stars, this.area, this.reviews, this.roomImage, this.startDate, 
            this.endDate, this.available, this.bookings);                               // for rooms where dates are given
        }
    }
    // Prints Room obj's propertires as a string
    public String toString()
    {
        String formattedStars = String.format("%.2f", this.stars);
        return "Name: " + this.roomName + "\n" +
               "Area: " + this.area + "\n" +         
               "Number of guests: " + this.guests + "\n" + 
               "Price: " + this.price + "\n" + 
               "Stars: " + formattedStars + "\n" +
               "Reviews: " + this.reviews + "\n" +
               "Start date: " + this.startDate + "\n" +
               "End date: " + this.endDate + "\n" +
               "Bookings: " + this.bookings;
    }
}