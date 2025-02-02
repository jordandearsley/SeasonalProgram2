package Components.Tables;

import Components.Exceptions.InvalidInputException;
import Components.Security;
import Components.Series;
import Components.Table;
import Components.Trade;
import Components.Exceptions.SymbolInvalidException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class YearlyPerformanceTable extends Table {
    public YearlyPerformanceTable(List<Security> securities, Security benchmark, Calendar startDate, Calendar endDate) throws SymbolInvalidException, InvalidInputException {
        int startYear = startDate.get(1);
        int endYear = endDate.get(1);
        List<String> titleRow = new ArrayList();
        titleRow.add("Sectors Performance");
        titleRow.add(endYear + " % Gain");
        titleRow.add(startYear + "-" + endYear + " Average % Gain");
        titleRow.add(startYear + "-" + endYear + " Fq % Positive");
        titleRow.add(startYear + "-" + endYear + " Fq % Positive > " + benchmark.getSymbol());
        this.addRow(titleRow);

        Calendar fullStart = new GregorianCalendar(startYear,0,1);
        Calendar fullEnd = new GregorianCalendar(endYear+1,0,1);

        benchmark.forceRefresh(fullStart, fullEnd);

        for(Security s: securities) {

            s.forceRefresh(fullStart, fullEnd);

            ArrayList row = new ArrayList();
            row.add(s.getSymbol());

            Calendar start = new GregorianCalendar(startYear, 0, 1);
            Calendar end = new GregorianCalendar(endYear, 0, 1);

            // set it to the end of the year
            end.set(Calendar.DAY_OF_YEAR, end.getActualMaximum(Calendar.DAY_OF_YEAR));

            Trade t = new Trade(true, start, end, s, benchmark);

            Series gains = t.getGains();
            Series gainsVsBenchmark = t.getDiffGains();

            row.add(this.round((Double)gains.getValues().get(gains.getValues().size() - 1)).toString());
            row.add(this.round(gains.getAverage()).toString());
            row.add(this.round(gains.getFreqPositive()).toString());
            row.add(this.round(gainsVsBenchmark.getFreqPositive()).toString());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);

            if (s.getCloses().getDates().size() == 0) {
                row.add("* No data available");

            } else{
                if(s.getCloses().getDates().get(0).compareTo(fullStart) > 0) {
                    String startDateString = sdf.format(s.getCloses().getDates().get(0).getTime());
                    row.add("* Some data unavailable, starting " + startDateString);
                }
                if (s.getCloses().getDates().get(s.getCloses().getDates().size()-1).before(fullEnd)) {
                    String endDateString = sdf.format(s.getCloses().getDates().get(s.getCloses().getDates().size()-1).getTime());
                    row.add("* ending " + endDateString);
                }
            }

            this.addRow(row);
        }

    }
}
