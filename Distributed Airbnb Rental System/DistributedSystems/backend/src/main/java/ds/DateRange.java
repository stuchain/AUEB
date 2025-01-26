package ds;
import java.time.LocalDate;

public class DateRange  
{
    private LocalDate startDate;                                            // start date of date range
    private LocalDate endDate;                                              // end date of date range
    
    // Getters
    public LocalDate getStartDate()                                         // get the starting date
    {
        return startDate;
    }

    public LocalDate getEndDate()                                           // get the ending date
    {
        return endDate;
    }
    //Setters
    public void setStartDate(String startDate)                              // set the starting date
    {
        this.startDate = LocalDate.parse(startDate);                        // parse a given string to local date object
    }
    public void setEndDate(String endDate)                                  // set the ending date
    {
        this.endDate = LocalDate.parse(endDate);                            // parse a given string to local date object
    }
    // Checks if a given daterange is within another
    public boolean isWithinRange(DateRange other) 
    {
        return (this.startDate.isAfter(other.startDate) || this.startDate.isEqual(other.startDate)) && 
                (this.endDate.isBefore(other.endDate) || this.endDate.isEqual(other.endDate));
    }
    // Checks if there is a conflict between two date ranges
    public boolean conflictOfBooking(DateRange other){
        return ((this.startDate.isAfter(other.startDate) || this.startDate.isEqual(other.startDate)) && 
                (this.startDate.isBefore(other.endDate) || this.startDate.isEqual(other.endDate))||
                (this.endDate.isAfter(other.startDate) || this.endDate.isEqual(other.startDate)) && 
                (this.endDate.isBefore(other.endDate) || this.endDate.isEqual(other.endDate)));
    }
    // Override equals method to compare if a given data range object 
    // is within the range of this data range object
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || DateRange.class != obj.getClass()) {             // if given obj is null or its class isn't DataRange return false
            return false;
        }
        if (this.getStartDate() == null || this.getEndDate() == null) {     // if one of the properties, of the object that the method is called to, is null
            return false;                                                   // return false
        }
        DateRange other = (DateRange) obj;                                  // cast given object to data range object
        return this.isWithinRange(other);                                   // return boolean value of isWithinRange method
    }   
    // Give the DateRange object's properties as a string
    @Override
    public String toString()
    {                                            
        return "[" + startDate + " - " + endDate + "]";
    }
}
