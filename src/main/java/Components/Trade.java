
package Components;

import Components.Exceptions.InvalidInputException;
import Components.Exceptions.SymbolInvalidException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class Trade implements Serializable {
    private static final long serialVersionUID = 4L;
    SimpleDateFormat sdf;
    boolean isLong;
    Security security;
    Security benchmark;
    int startYear;
    int endYear;
    int startMonth;
    int endMonth;
    int startDay;
    int endDay;
    boolean endOneDayPrevious;

    public Trade(boolean isLong, String start, String end, Security security, Security benchmark, boolean endOneDayPrevious) {
        this.sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        this.isLong = isLong;
        this.security = security;
        this.benchmark = benchmark;
        this.setStart(start);
        this.setEnd(end);
    }

    public Trade(boolean isLong, Calendar start, Calendar end, Security security, Security benchmark) {
        this(isLong, start, end, security, benchmark, false);
    }

    public Trade(boolean isLong, Calendar start, Calendar end, Security security, Security benchmark, boolean endOneDayPrevious) {
        this.sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        this.isLong = isLong;
        this.security = security;
        this.benchmark = benchmark;
        this.setStart(start);
        this.setEnd(end);
        this.endOneDayPrevious = endOneDayPrevious;
    }

    public Trade(Security benchmark) {
        this.sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        this.isLong = true;
        this.security = new Security();
        this.benchmark = benchmark;
        this.setStart("1999/12/31");
        this.setEnd("1999/12/31");
        this.endOneDayPrevious = false;
    }

    Series getSecurityGains(Security s) throws SymbolInvalidException, InvalidInputException {
        Calendar securityStart = new GregorianCalendar(this.startYear, this.startMonth - 1, this.startDay);
        Calendar securityEnd = new GregorianCalendar(this.endYear, this.endMonth - 1, this.endDay);

        s.refresh(securityStart, securityEnd);

        int firstPeriodStartYear = Math.max(this.startYear, s.getDataStart().get(1));
        int lastPeriodEndYear = Math.min(this.endYear, s.getDataEnd().get(1));
        Calendar start = new GregorianCalendar(firstPeriodStartYear, this.startMonth - 1, this.startDay);
        Calendar end = new GregorianCalendar(firstPeriodStartYear, this.endMonth - 1, this.endDay);
        if (start.compareTo(end) >= 0) {
            end.add(1, 1);
        }

        List<Calendar> dates = new ArrayList();
        ArrayList values = new ArrayList();

        while(end.get(1) <= lastPeriodEndYear) {
            Double startingValue = s.getCloses().getValue(start);

            Double endingValue;
            if(this.endOneDayPrevious){
                endingValue = s.getCloses().getValue(end);
            }else{
                endingValue = s.getCloses().getCurrentValue(end);
            }

            dates.add((Calendar)start.clone());
            Double finalValue = endingValue / startingValue - 1.0D;
            values.add(this.isLong || s == benchmark ? finalValue : -1.0D * finalValue);
            start.add(1, 1);
            end.add(1, 1);
        }

        return new Series(s.getSymbol() + " Gains", dates, values);
    }

    public Series getGains() throws SymbolInvalidException, InvalidInputException {
        return this.getSecurityGains(this.security);
    }

    public Series getBenchmarkGains() throws SymbolInvalidException, InvalidInputException {
        return this.getSecurityGains(this.benchmark);
    }

    public Series getDiffGains() throws SymbolInvalidException, InvalidInputException {
        return this.getGains().getDiffVs(this.getBenchmarkGains());
    }

    public boolean isLong() {
        return this.isLong;
    }

    public boolean setIsLong(boolean isLong) {
        this.isLong = isLong;
        return this.isLong;
    }

    public String getStart() {
        return this.startYear + "/" + this.startMonth + "/" + this.startDay;
    }

    public String getEnd() {
        return this.endYear + "/" + this.endMonth + "/" + this.endMonth;
    }

    public Calendar getStartCal() {
        return new GregorianCalendar(this.startYear, this.startMonth - 1, this.startDay);
    }

    public Calendar getEndCal() {
        return new GregorianCalendar(this.endYear, this.endMonth - 1, this.endDay);
    }

    public void setStart(String start) {
        String[] parts = start.split("/");
        this.startYear = Integer.valueOf(parts[0]);
        this.startMonth = Integer.valueOf(parts[1]);
        this.startDay = Integer.valueOf(parts[2]);
    }

    public void setEnd(String end) {
        String[] parts = end.split("/");
        this.endYear = Integer.valueOf(parts[0]);
        this.endMonth = Integer.valueOf(parts[1]);
        this.endDay = Integer.valueOf(parts[2]);
    }

    public void setStart(Calendar start) {
        this.startYear = start.get(1);
        this.startMonth = start.get(2) + 1;
        this.startDay = start.get(5);
    }

    public void setEnd(Calendar end) {
        this.endYear = end.get(1);
        this.endMonth = end.get(2) + 1;
        this.endDay = end.get(5);
    }

    public Security getSecurity() {
        return this.security == null ? new Security("None") : this.security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Security getBenchmark() {
        return this.benchmark;
    }

    public String getTradeWindow(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        Calendar c = Calendar.getInstance();
        String out = "";
        c.set(2, this.startMonth - 1);
        c.set(5, this.startDay);
        out = out + sdf.format(c.getTime()) + "-";
        c.set(2, this.endMonth - 1);
        c.set(5, this.endDay);
        out = out + sdf.format(c.getTime());
        return out;
    }

    public int getNumPeriods(){
        if(this.startMonth < this.endMonth || (this.startMonth == this.endMonth && this.startDay <= this.endDay)){
            return this.endYear - this.startYear + 1;
        }else {
            return this.endYear - this.startYear;
        }
    }
}
