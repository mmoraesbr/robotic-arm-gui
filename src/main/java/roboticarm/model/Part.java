package roboticarm.model;

/**
 * Created by mmoraes on 18/06/17.
 */
public enum Part {
    Craw('C', "Craw", 40, 127), Arm('A', "Braço", 0, 180), Base('B', "Base", 0, 180), Elevator('E', "Elevator", 0, 180);

    final char code;
    final String name;
    final int max;
    final int min;

    Part(char code, String name, int min, int max) {
        this.code = code;
        this.name = name;
        this.min = min;
        this.max = max;
    }

    public char getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean accept(int position) {
        return (position >= min) && (position <= max);
    }
}
