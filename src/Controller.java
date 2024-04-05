import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Serves as the main class for the song. Loads in the note names from the designated file and "conducts" different
 * threads to play at the correct times, playing the notes loaded from the file.
 *
 * @author Ryan Johnson
 */
public class Controller {
    private final List<String[]> noteList;
    private final HashMap<String, Bell> bells;

    /**
     * Initializes the Controller class, creating an empty list of notes and hashmap to map a note name to its Bell object.
     */
    public Controller() {
        this.noteList = new ArrayList<>();
        this.bells = new HashMap<>();
    }

    /**
     * Converts a file containing musical note names/lengths into a list of BellNote objects. These BellNote objects are
     * later used to play the notes as audio.
     *
     * @param fileName String detailing the name of the file containing the note names/lengths
     * @return true if the notes from the file are loaded correctly; false otherwise
     */
    private boolean importFile(String fileName) {
        try (Scanner notesScanner = new Scanner(new File(fileName))) {
            int lineCounter = 0;
            // Ensures the file is a .txt file
            boolean fileValid = true;
            if (!Objects.equals(getFileExtension(fileName), "txt")) {
                System.err.println("ERROR: Song file must be a .txt file");
                fileValid = false;
            }

            // Creates a bell object for every unique note name in the file
            while (notesScanner.hasNextLine()) {
                lineCounter++;
                // Account for multiple spaces between note arguments and leading/trailing spaces
                String lineOneSpace = notesScanner.nextLine().trim().replaceAll("\\s+", " ");
                String[] noteData = lineOneSpace.split(" ");

                // Validate the file
                if (!validateLineData(lineCounter, lineOneSpace)) {
                    fileValid = false;
                    continue;
                }

                String noteName = noteData[0];
                String noteLength = convertNoteLength(noteData);

                noteList.add(new String[]{noteName, noteLength});
                if (!bells.containsKey(noteName)) {
                    bells.put(noteName, new Bell(noteName));
                }
            }
            return fileValid;
        } catch (FileNotFoundException e) {
            System.err.printf("ERROR: %s could not be found.", fileName);
            return false;
        }
    }

    /**
     * Returns the extension of a file. Used for ensuring a song file is of the correct file format.
     *
     * @param fileName String containing the name of the song file
     * @return String containing the extension of the file
     */
    public String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf(".");
        return fileName.substring(lastDotIndex + 1);
    }

    /**
     * Converts the length of a note from an integer string to the spelled-out version of the note length (1 -> WHOLE).
     *
     * @param noteData String[] containing the note name and length
     * @return String specifying the spelled-out length of the note
     */
    private String convertNoteLength(String[] noteData) {
        String noteLength = noteData[1];

        // Converts the number representing the note length to the kind of note
        noteLength = switch (noteLength) {
            case "1" -> "WHOLE";
            case "2" -> "HALF";
            case "3" -> "TRIPLET";
            case "4" -> "QUARTER";
            case "8" -> "EIGHTH";
            case "16" -> "SIXTEENTH";
            default -> noteData[1];
        };
        return noteLength;
    }

    /**
     * Validates that a line from the input file contains valid note name and length data. The input line must have all
     * spaces removed except for the single space between arguments.
     *
     * @param line  String containing a single line from the input file. This line cannot contain leading/trailing
     *              white space or multiple spaces between arguments.
     * @return true if the data within the line is correct; false otherwise
     */
    public boolean validateLineData(int lineNum, String line) {
        boolean successful = true;

        String[] noteData = line.split(" ");

        // Validate that there are no empty lines
        if (noteData[0].isEmpty()) {
            System.err.printf("ERROR: An empty line cannot be contained within the song file (line %d)\n", lineNum);
            successful = false;
        }

        // Validate that there are two arguments provided for the note
        if (noteData.length < 2) {
            System.err.printf("ERROR: Less than 2 arguments provided for a note (line %d)\n", lineNum);
            return false;
        }

        // Validate that only two arguments are provided for each note
        if (noteData.length > 2) {
            System.err.printf("ERROR: More than 2 arguments provided for a note (%d)\n", lineNum);
            return false;
        }

        String noteName = noteData[0];
        String noteLength = noteData[1];

        // Validates the note name
        try {
            Bell.NoteName.valueOf(noteName);
        } catch (IllegalArgumentException e) {
            System.err.printf("ERROR: %s is not a valid note name (line %d)\n", noteName, lineNum);
            successful = false;
        }

        // Validates the note length
        List<String> validLengths = new ArrayList<>(List.of("1", "2", "3", "4", "8", "16"));
        if (!validLengths.contains(noteLength)) {
            System.err.printf("ERROR: %s is not a valid note length (line %d)\n", noteLength, lineNum);
            successful = false;
        }

        return successful;
    }

    /**
     * Plays the notes from the input file after starting each of the bell threads. Each bell has an assigned note,
     * being called to play that note at the correct location within the song. Directly before calling the bell, its
     * note length is set to specify how long the note should play for. Once all the notes from the input file have
     * been played, each of the threads is stopped.
     */
    public void playSong() {
        // Start each of the bell threads
        for (Bell bell : bells.values()) {
            bell.startRunning();
        }

        // Create the audio output
        final AudioFormat af = new AudioFormat(Bell.NoteName.SAMPLE_RATE, 8, 1, true, false);

        try (final SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            System.out.printf("Line: %s", line);
            line.open();
            line.start();

            // Set the same audio line for each unique bell
            for (Bell bell : bells.values()) {
                bell.setLine(line);
            }

            // Play each note listed in the file
            for (String[] note : noteList) {
                Bell bell = bells.get(note[0]);
                NoteLength noteLength = NoteLength.valueOf(note[1]);
                bell.setNoteLength(noteLength);
                bell.giveTurn();
            }

            stopBells();

            line.drain();
        } catch (LineUnavailableException e) {
            throw new RuntimeException("Line unavailable");
        }
    }

    /**
     * Stops each of the separate bell threads. First, each of the threads must be notified that it is time to stop, after
     * which each thread is stopped only once it has finished its execution.
     */
    public void stopBells() {
        try {
            for (Bell bell : bells.values()) {
                bell.stopRunning();
            }

            for (Bell bell : bells.values()) {
                bell.getThread().join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Error while stopping bell threads");
        }
    }

    public static void main(String[] args) {
        Controller controller = new Controller();
        if (!controller.importFile(args[0])) {
            return;
        }

        controller.playSong();
    }
}
