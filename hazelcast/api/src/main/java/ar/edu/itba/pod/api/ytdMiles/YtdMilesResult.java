package ar.edu.itba.pod.api.ytdMiles;

public record YtdMilesResult(
    String company,
    int year,
    int month,
    double milesYTD
) implements Comparable<YtdMilesResult> {
    
    @Override
    public String toString() {
        double truncatedMiles = Math.floor(milesYTD * 100) / 100.0;
        
        return String.format("%s;%d;%d;%.2f", 
            company, 
            year, 
            month, 
            truncatedMiles
        );
    }

    @Override
    public int compareTo(YtdMilesResult other) {
        int companyComp = this.company.compareTo(other.company);
        if (companyComp != 0) return companyComp;
        
        int yearComp = Integer.compare(this.year, other.year);
        if (yearComp != 0) return yearComp;
        
        return Integer.compare(this.month, other.month);
    }

    public String getKey() {
        return String.format("%s;%d;%d", company, year, month);
    }
}

