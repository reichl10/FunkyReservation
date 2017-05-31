package funky.pom16.funkyreservation.backend.data;

/**
 * Created by manuel on 31.05.16.
 */
public class Table {
    private int number;
    private int persons;

    public Table(int number){
        this.number = number;
        this.persons = 4;
    }

    public Table(int number, int persons){
        this.number = number;
        this.persons = persons;
    }

    public int getPersons() {
        return this.persons;
    }

    public void setPersons(int persons){
        this.persons = persons;
    }

    public String toString(){
        return "Table #" + this.number;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
