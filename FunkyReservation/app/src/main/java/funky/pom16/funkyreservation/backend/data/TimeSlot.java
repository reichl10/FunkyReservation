package funky.pom16.funkyreservation.backend.data;

import java.util.Calendar;

/**
 * Created by manuel on 01.06.16.
 */
public class TimeSlot {
    private Calendar startTime;
    private Calendar endTime;
    private Table table;
    private int persons;

    public TimeSlot(Calendar startTime, Calendar endTime, Table table, int persons) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.table = table;
        this.persons = persons;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }


    public int getPersons() {
        return persons;
    }

    public void setPersons(int persons) {
        this.persons = persons;
    }

    public String toString(){
        return "Time-slot (table|from|until): " + this.table.getNumber() + " | " +
                this.startTime.getTime() + " | " + this.endTime.getTime();
    }
}
