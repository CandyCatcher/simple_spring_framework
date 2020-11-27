package top.candysky.demo;

public class Demo {

    public static void main(String[] args) {
        Weekday weekday = Weekday.SUN;
        //if (weekday == Weekday.SAT || weekday == Weekday.SUN) {
        //    System.out.println("work at home");
        //}
        System.out.println(Weekday.MON.name());
        System.out.println(Weekday.TUE.ordinal());
        System.out.println(3 == Weekday.WED.dayValue);
        System.out.println(3 == Weekday.THU.dayValue);
        System.out.println(Entry.HOLDER.man.toString());
    }

    public static Man getMan() {
        return Entry.HOLDER.man;
    }

    enum Entry {
        //man是holder的属性
        HOLDER;

        private Man man;

        Entry() {
            man = new Man();
        }
    }
}

enum Weekday {
    SUN(0), MON(1), TUE(2), WED(3), THU(4), FRI(5), SAT(6);

    public final int dayValue;

    Weekday(int dayValue) {
        this.dayValue = dayValue;
    }
}


