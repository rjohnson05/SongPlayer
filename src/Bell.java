import javax.sound.sampled.SourceDataLine;

/**
 * Contains all the data necessary to play a musical note for a specified amount of time. Each Bell object is assigned
 * a single musical note to play on its turn. The bell also contains the length that the note should be played for. This
 * note length will probably change each time the bell receives a turn to play. Each Bell object runs as a separate
 * thread. The controller determines when each bell object should play its note.
 *
 * @author Ryan Johnson
 */
public class Bell implements Runnable {
    /**
     * Stores all possible note names, as well as the ability to translate these note names into the correct sound frequencies.
     */
    enum NoteName {
        // REST Must be the first 'Note'
        REST,
        E3,
        F3,
        F3S,
        G3,
        G3S,
        A3,
        A3S,
        B3,
        C4,
        C4S,
        D4,
        D4S,
        E4,
        F4,
        F4S,
        G4,
        G4S,
        A4,
        A4S,
        B4,
        C5,
        C5S,
        D5,
        D5S,
        E5,
        F5,
        F5S,
        G5,
        G5S,
        A5,
        A5S,
        B5,
        C6;

        public static final int SAMPLE_RATE = 48 * 1024; // ~48KHz
        public static final int MEASURE_LENGTH_SEC = 2;

        // Circumference of a circle divided by # of samples
        private static final double step_alpha = (2.0d * Math.PI) / SAMPLE_RATE;

        private final double FREQUENCY_A_HZ = 164.81d;
        private final double MAX_VOLUME = 127.0d;

        private final byte[] sinSample = new byte[MEASURE_LENGTH_SEC * SAMPLE_RATE];

        /**
         * Initializes a NoteName, including setting its frequency and sinusoidal data.
         */
        NoteName() {
            int n = this.ordinal();
            if (n > 0) {
                // Calculate the frequency!
                final double halfStepUpFromA = n - 1;
                final double exp = halfStepUpFromA / 12.0d;
                final double freq = FREQUENCY_A_HZ * Math.pow(2.0d, exp);

                // Create sinusoidal data sample for the desired frequency
                final double sinStep = freq * step_alpha;
                for (int i = 0; i < sinSample.length; i++) {
                    sinSample[i] = (byte)(Math.sin(i * sinStep) * MAX_VOLUME);
                }
            }
        }
    }

    private volatile boolean running;
    private boolean myTurn;
    private final NoteName noteName;
    private NoteLength noteLength;
    private SourceDataLine line;
    private final Thread thread;

    /**
     * Constructor for the Bell class. Accepts the bell's note name as a parameter.
     *
     * @param noteName String containing the name of note the bell will be playing
     */
    public Bell(String noteName) {
        this.noteName = NoteName.valueOf(noteName);
        this.thread = new Thread(this, noteName);
        this.running = true;
        this.myTurn = false;
    }

    /**
     * Plays the bell's assigned note audibly for the specified amount of time.
     *
     * @param noteLength NoteLength object specifying the length of time to play the note
     */
    public void playNote(NoteLength noteLength) {
        final int ms = Math.min(noteLength.timeMs(), Bell.NoteName.MEASURE_LENGTH_SEC * 1000);
        final int length = Bell.NoteName.SAMPLE_RATE * ms / 1000;
        line.write(this.sample(), 0, length);
    }

    /**
     * Gives a single turn to the bell, allowing to play its note for the specified amount of time. Once its turn is
     * over, the thread goes back into waiting.
     */
    public void giveTurn() {
        synchronized (this) {
            if (myTurn) {
                throw new IllegalStateException("Attempt to give a turn to a player who's hasn't completed the current turn");
            }
            myTurn = true;
            notify();
            while (myTurn) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }
        }
    }

    /**
     * Starts the thread execution of the bell.
     */
    public void startRunning() {
        thread.start();
    }

    /**
     * Stops the execution of the bells. Prompts the thread to finish the run() method and terminate.
     */
    public void stopRunning() {
        System.out.println("Setting running to false on thread" + thread);
        running = false;
        myTurn = true;
        synchronized (this) {
            notify();
        }
    }

    /**
     * Serves as the main method for each thread. The bell remains in waiting until it receives the signal that it is time
     * to play its note. After playing its note, it goes back into waiting. This loop is executed until the thread
     * receives notice to shut down.
     */
    public synchronized void run() {
        while (running) {
            while (!myTurn) {
                try {
                    wait();
                } catch (InterruptedException ignored) {}
            }
            if (running) {
                playNote(noteLength);
                myTurn = false;
                notify();
            }
        }
    }

    /**
     * Returns the audio sample for the bell.
     *
     * @return byte[] containing the sinusoidal audio data for the bell
     */
    public byte[] sample() {
        return noteName.sinSample;
    }

    /**
     * Sets the length of the note to the desired length, such as a quarter note, eighth note, etc.
     *
     * @param noteLength  NoteLength object specifying the length of the note
     */
    public void setNoteLength(NoteLength noteLength) {
        this.noteLength = noteLength;
    }

    /**
     * Sets the audio line for the bell.
     *
     * @param line SourceDataLine object containing the desired audio line
     */
    public void setLine(SourceDataLine line) {
        this.line = line;
    }

    /**
     * Returns the thread for the bell.
     *
     * @return Thread object specifying the thread running the bell object
     */
    public Thread getThread() {
        return thread;
    }
}


