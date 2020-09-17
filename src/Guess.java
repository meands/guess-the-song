import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Guess {

    // initialise a hashmap to store song data
    static HashMap<String, String[]> data = new HashMap<>();
    // initialise a hashmap to store the artists
    static HashMap<String, ArrayList<String>> artists = new HashMap<>();

    public static void main(String[] args) {
        // process the list of songs
        process();
        goThroughArtists();
        // start quiz
        startQuiz(data);
    }

    /**
     * The supplied text file contains songs and associate comments which describes them.
     * Parse and store the data.
     */
    public static void process() {
        try {
            // read file line by line
            File file = new File("songs.txt");
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNextLine()) {
                String rawData = fileReader.nextLine();
                String[] content = rawData.split(";");
                // format of each line in text file: comment;song;artist
                String comment = content[0];
                String song = content[1];
                String artist = content[2];
                // create string array to store the song name and artist, this uniquely identifies a song
                String[] songAndArtist = new String[2];
                songAndArtist[0] = song;
                songAndArtist[1] = artist;
                // add to hashmap
                data.put(comment, songAndArtist);
                // if this artist is not already in the hashmap of artists, add them to it
                if (!artists.containsKey(artist)) {
                    ArrayList<String> commentKeys = new ArrayList<>();
                    commentKeys.add(comment);
                    artists.put(artist, commentKeys);
                }
                // if this artist is already in the hashmap of artist, add the comment to the value
                else {
                    ArrayList<String> commentKeys = artists.get(artist);
                    commentKeys.add(comment);
                    artists.replace(artist, commentKeys);
                }
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * The user is asked which artists they recognise, songs of artists the user doesn't recognise will not appear in the quiz.
     */
    public static void goThroughArtists() {
        System.out.println("Select artists you recognise to start the quiz");
        System.out.println("Type Y if you recognise the artist and N if you don't:");
        for (Map.Entry<String, ArrayList<String>> artists : artists.entrySet()){
            String artist = artists.getKey();
            ArrayList<String> commentKeys = artists.getValue();
            System.out.println(artist);
            String input = getValidInput();
            // if the user doesn't recognise this artist, remove the song data of this artist from the songs hashmap
            if (input.equals("N")) {
                for (String commentKey : commentKeys) {
                    data.remove(commentKey);
                }
            }
        }
    }

    /**
     * Get valid user input.
     * @return either Y or N
     */
    public static String getValidInput() {
        Scanner scan = new Scanner(System.in);
        String input = scan.next();
        while (!input.equals("Y") && !input.equals("N")) {
            System.out.println("Please type either Y or N.");
            input = scan.next();
        }
        return input;
    }
    /**
     * Check the user input against the correct answer.
     * @param indexOfCorrectAnswer the index of the correct answer (0-3)
     */
    public static void checkAnswer(int indexOfCorrectAnswer) {
        Scanner scan = new Scanner(System.in);
        int input = scan.nextInt();
        // make sure user input is valid
        while (input > 4 || input < 1){
            System.out.println("Invalid answer, try again.");
            input = scan.nextInt();
        }
        if (indexOfCorrectAnswer == input - 1) {
            System.out.println("Correct!");
        }
        else {
            System.out.println("Incorrect. The correct answer is " + (indexOfCorrectAnswer + 1) + ".");
            System.out.println();
        }
    }

    /**
     * Quiz the user.
     * @param data hashmap which contains the song data
     */
    public static void startQuiz(HashMap<String, String[]> data) {
        List<String[]> songsAndArtists = new ArrayList<>(data.values());
        // taken from https://crunchify.com/java-how-to-get-random-key-value-element-from-hashmap/
        // get random key-value pair (comment-song info) from hashmap
        List<Map.Entry<String, String[]>> list = new ArrayList<>(data.entrySet());
        Collections.shuffle(list);
        for (Map.Entry<String, String[]> d : list) {
            String comment = d.getKey();
            String[] answer = d.getValue();
            System.out.println("What song was this commented on?");
            System.out.println(comment);
            int indexOfCorrectAnswer = displayAnswers(songsAndArtists, answer);
            checkAnswer(indexOfCorrectAnswer);
            System.out.println("Would you like to continue? (Y for yes and N for no)");
            String input = getValidInput();
            if (input.equals("N")) {
                System.out.println("Thank you for playing!");
                return;
            }
        }
        System.out.println("Quiz complete. Thank you for playing!");
    }

    /**
     * Display possible answers.
     * @param songsAndArtists list of arrays containing song names and artists
     * @param answer correct answer
     * @return index of correct answer
     */
    public static int displayAnswers(List<String[]> songsAndArtists, String[] answer) {
        // correct answer is in index 0 and the shuffled answers are in index 1
        Object[] result = shuffleAnswers(songsAndArtists, answer);
        String[][] shownAnswers = (String[][]) result[1];
        System.out.println("1. " + shownAnswers[0][0] + " by " + shownAnswers[0][1]);
        System.out.println("2. " + shownAnswers[1][0] + " by " + shownAnswers[1][1]);
        System.out.println("3. " + shownAnswers[2][0] + " by " + shownAnswers[2][1]);
        System.out.println("4. " + shownAnswers[3][0] + " by " + shownAnswers[3][1]);
        return (int) result[0];
    }

    /**
     * Shuffle the answers while keeping track of the index of the correct answer.
     * @param songsAndArtists list of arrays containing song names and artists
     * @param answer correct answer
     * @return object array containing index of the correct answer and all the answers being displayed
     */
    public static Object[] shuffleAnswers(List<String[]> songsAndArtists, String[] answer) {
        String[][] shownAnswers = new String[4][];
        // the correct answer is stored in the first index
        shownAnswers[0] = answer;
        List<String[]> wrongAnswers = getRandomSongs(songsAndArtists, answer);
        shownAnswers[1] = wrongAnswers.get(0);
        shownAnswers[2] = wrongAnswers.get(1);
        shownAnswers[3] = wrongAnswers.get(2);
        // taken from https://stackoverflow.com/questions/1519736/random-shuffling-of-an-array
        Random rnd = ThreadLocalRandom.current();
        int correctIndex = 0;
        for (int i = 3; i >= 0; i--) {
            int index = rnd.nextInt(i + 1);
            // simple swap
            String[] temp = shownAnswers[index];
            shownAnswers[index] = shownAnswers[i];
            shownAnswers[i] = temp;
            // keep track of the index of the correct answer
            if (correctIndex == index) {
                correctIndex = i;
            }
        }
        Object[] answersAndCorrectIndex = new Object[2];
        answersAndCorrectIndex[0] = correctIndex;
        answersAndCorrectIndex[1] = shownAnswers;
        return answersAndCorrectIndex;
    }

    /**
     * Choose 3 random songs to be the wrong answers.
     * @param songsAndArtists list of arrays containing song names and artist
     * @param answer correct answer
     * @return list of wrong answers.
     */
    public static List<String[]> getRandomSongs(List<String[]> songsAndArtists, String[] answer) {
        // taken from https://stackoverflow.com/questions/8378752/pick-multiple-random-elements-from-a-list-in-java
        List<String[]> copy = new LinkedList<>(songsAndArtists);
        Collections.shuffle(copy);
        List<String[]> wrongAnswers = new LinkedList<>();
        int count = 0, index = 0;
        while (count < 3) {
            String[] thisAnswer = copy.get(index);
            if (!Arrays.deepEquals(thisAnswer, answer)) {
                wrongAnswers.add(thisAnswer);
                count++;
            }
            index++;
        }
        return wrongAnswers;
    }
}
