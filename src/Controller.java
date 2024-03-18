import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Serves as the main class for the song player. Loads in the note names from the designated file and "conducts" different
 * threads to play at the correct times, playing the notes loaded from the file.
 *
 * @author Ryan Johnson
 */
public class Controller implements Runnable {
    private Set<Bell> bells;

    public Controller() {
        this.bells = new HashSet<>();
    }

    /**
     * Converts a file containing musical note names/lengths into a list of BellNote objects.
     *
     * @param fileName String detailing the name of the file containing the note names/lengths
     * @return true if the notes from the file are loaded correctly; false otherwise
     */
    public boolean importFile(String fileName) {
        Scanner notesScanner = null;
        try {
            File notesFile = new File(fileName);
            notesScanner = new Scanner(notesFile);

            while (notesScanner.hasNextLine()) {
                String[] noteData = notesScanner.nextLine().split(", ");
                if (Objects.equals(noteData[0], "")) {
                    continue;
                }
                String noteName = noteData[0];
                Bell bell = new Bell(noteName);
//                NoteLength noteLength = NoteLength.valueOf(noteData[1]);
                bells.add(bell);
            }
            System.out.printf("Bells: %s", bells);

            notesScanner.close();
            return true;
        } catch (FileNotFoundException e) {
            System.out.printf("%s could not be found.", fileName);
            return false;
        }
    }

    public void run() {}

    public static void main(String[] args) throws Exception {
        Controller controller = new Controller();
        if (!controller.importFile(args[0])) {
            return;
        };

        final AudioFormat af =
                new AudioFormat(Bell.NoteName.SAMPLE_RATE, 8, 1, true, false);
        Tone t = new Tone(af);
//        t.playSong(controller.songNotes);
    }
}
