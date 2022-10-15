public class BrandRanking {

    public String brand;
    public String position;
    public String previousPosition;
    public String brandValue;
    public String previousBrandValue;
    public String rating;
    public String previousRating;
    public String year;
    public String previousYear;

    public BrandRanking(String values) {
        this(values.split(","));
    }

    public BrandRanking(String[] array) {
        this.brand = array[0];
        this.position = array[1];
        this.previousPosition = array[2];
        this.brandValue = array[3];
        this.previousBrandValue = array[4];
        this.rating = array[5];
        this.previousRating = array[6];
        this.year = array[7];
        this.previousYear = array[8];
    }

    @Override
    public String toString() {
        return brand + ","
                + position + ","
                + previousPosition + ","
                + brandValue + ","
                + previousBrandValue + ","
                + rating + ","
                + previousRating + ","
                + year + ","
                + previousYear;
    }


}
