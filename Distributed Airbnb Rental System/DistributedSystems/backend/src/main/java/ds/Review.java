package ds;

import java.lang.reflect.Field;

public class Review extends Request {
    private double reviewStars;
    private String roomForReview;

    // Default Constructor
    Review()
    {
    }
    // Constructor
    Review(double rev, String room){
        this.reviewStars=rev;
        this.roomForReview=room;
    }
    // Setters
    public void setReview(double r){
        this.reviewStars = r;
    }
    public void setRoomForReview(String ro){
        this.roomForReview = ro ;
    }
    // Getters 
    public double getReview(){
        return reviewStars ;
    }
    public String getRoomForReview(){
        return roomForReview ;
    }
    //  Computes how many non null or non 0 values does this object has
    public int numNonZero() {
        int count = 0;
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(this);
                if(value != null && !value.equals(0) && !value.equals(0.0)) {    // if value is not null/0/0.0
                    count++;                                                     // increase count
                }
            } catch (IllegalAccessException e) {                                
                e.printStackTrace();
            }
        }
        return count;
    }
    // Prints Review obj's properties as a String
    public String toString()
    {
        return "[" + this.reviewStars + ", " + this.roomForReview + "]";
    }
}