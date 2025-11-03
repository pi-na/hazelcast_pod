package ar.edu.itba.pod.api.ytdMiles;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.Objects;

public class CompanyYearMonth implements DataSerializable, Comparable<CompanyYearMonth> {
    
    private String company;
    private int year;
    private int month;

    public CompanyYearMonth() {
        // Default constructor for deserialization
    }

    public CompanyYearMonth(String company, int year, int month) {
        this.company = company;
        this.year = year;
        this.month = month;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(company);
        out.writeInt(year);
        out.writeInt(month);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        company = in.readUTF();
        year = in.readInt();
        month = in.readInt();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompanyYearMonth other)) return false;
        return company.equals(other.company) &&
               year == other.year &&
               month == other.month;
    }

    @Override
    public int hashCode() {
        return Objects.hash(company, year, month);
    }

    @Override
    public int compareTo(CompanyYearMonth other) {
        int companyComp = this.company.compareTo(other.company);
        if (companyComp != 0) return companyComp;
        
        int yearComp = Integer.compare(this.year, other.year);
        if (yearComp != 0) return yearComp;
        
        return Integer.compare(this.month, other.month);
    }

    public String getCompany() {
        return company;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }
}

