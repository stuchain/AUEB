package ds;

public class Booking extends Request {
        private DateRange date;                 // date range instance
        private String roomName;                // room name instance
        // Default Constructor
        Booking()
        {
            this.date = new DateRange();
            this.roomName = null ;
        }
        // Constructor
        Booking(String roomName, DateRange date){
            this.roomName=roomName;
            this.date=date;
        }
        // Setters
        public void setDate(String start, String end)
        {
            this.date.setStartDate(start);      // set start date of date range 
            this.date.setEndDate(end); 
        }
        public void setRoomName(String rn){
            this.roomName= rn ;
        }
        // Getters 
        public DateRange getDate(){
            return date ;
        }
        public String getRoomName(){
            return roomName ;
        }
        // Prints Review obj's properties as a String
        public String toString()
        {
            return "[" + this.roomName + ", " + this.date + "]";
        }
}    