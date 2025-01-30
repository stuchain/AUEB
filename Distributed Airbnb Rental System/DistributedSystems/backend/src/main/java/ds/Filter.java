package ds;
import java.lang.reflect.Field;

// Filters holder
public class Filter extends Request
{
    private String area;                          // area searching
    private DateRange dateRange;                  // daterange searching
    private int guests;                           // number of guests searching
    private double price;                         // price searching
    private int stars;                            // stars searching
    //Constructtor
    Filter()
    {
        this.area = null;
        this.dateRange = new DateRange();
        this.guests = 0;
        this.price = 0.0;
        this.stars = 0;
    }    
    // Setters for filter attributes
    public void setArea(String a)
    {
        this.area = a;
    }
    public void setDate(String start, String end)
    {
        this.dateRange.setStartDate(start);      // set start date of date range 
        this.dateRange.setEndDate(end);          // set end date of date range 
    }
    public void setGuests(int g)
    {
        this.guests = g;
    }
    public void setPrice(double p)
    {
        this.price = p;
    }
    public void setStars(int s)
    {
        this.stars = s;
    }
    // Getters of filter attributes
    public String getArea() {
        return area;
    }
    public DateRange getDate() {
        return dateRange;
    }
    public int getGuests() {
        return guests;
    }
    public double getPrice() {
        return price;
    }
    public int getStars() {
        return stars;
    }
    // Computes how many non null or 0 values does this object has
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
                } else if(value != null && !value.equals(0) && !value.equals(0.0)) {
                    count++;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return count;
    }
    // Prints Filter object's properties as a String
    public String toString()
    {
        return "[" + this.area + ", " + this.dateRange.toString() + ", " + this.guests + 
                ", " + this.price + ", " + this.stars + "]";
    }
}