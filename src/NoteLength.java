/**
 * Enumeration for the length of a note. Allows for whole, half, quarter, and eighth notes.
 *
 * @author Ryan Johnson
 */
public enum NoteLength {
    WHOLE(1.0f),
    HALF(0.5f),
    QUARTER(0.25f),
    TRIPLET(0.1667F),
    EIGHTH(0.125f),
    SIXTEENTH(0.0625F);

    private final int timeMs;

    /**
     * Initializes a NoteLength object, transforming the specified note length to the necessary number of milliseconds
     * it will play for.
     *
     * @param length  float specifying the type of length for the note (1.0f for whole, 0.5f for half, etc.)
     */
    NoteLength(float length) {
        timeMs = (int)(length * Bell.NoteName.MEASURE_LENGTH_SEC * 1000);
    }

    /**
     * Returns the number of milliseconds the note will play for.
     *
     * @return integer specifying the number of milliseconds the note will play for
     */
    public int timeMs() {
        return timeMs;
    }
}