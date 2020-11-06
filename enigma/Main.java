package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Pranav Bhimani
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named NAME. */
    private Scanner getInput(String name) {
        try {
            return new Scanner(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Return a PrintStream writing to the file named NAME. */
    private PrintStream getOutput(String name) {
        try {
            return new PrintStream(new File(name));
        } catch (IOException excp) {
            throw error("could not open %s", name);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String setLine = _input.nextLine();

        while (_input.hasNextLine()) {

            if (setLine.isEmpty()) {
                _output.println();
            }
            if (setLine.contains("*")) {
                setUp(enigma, setLine);
            } else {
                String next = setLine.replace(" ", "");
                for (int i = 0; i < next.length(); i++) {
                    if (!_alphabet.contains(next.charAt(i))) {
                        throw error("Input is not in Alphabet!");
                    }
                }
                printMessageLine(enigma.convert(next));
            }
            setLine = _input.nextLine();
        }

        if (!_input.hasNextLine()) {
            String lastLine = setLine.replace(" ", "");

            if (lastLine.isEmpty()) {
                _output.println();
            } else if (lastLine.contains("*")) {
                _output.print("");
            } else {
                for (int i = 0; i < lastLine.length(); i++) {
                    if (!_alphabet.contains(lastLine.charAt(i))) {
                        throw error("Input is not in Alphabet!");
                    }
                }
                printMessageLine(enigma.convert(lastLine));
            }
        }


    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alpha = _config.next();
            _alphabet = new Alphabet(alpha);
            if (_alphabet.contains('(') || _alphabet.contains(')')
                    || _alphabet.contains('*')) {
                throw new EnigmaException("Incorrect input for the alphabet!");
            }
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Next element must be an int!");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Next element must be an int!");
            }
            int numPawls = _config.nextInt();
            if (numPawls >= numRotors) {
                throw error("Cannot have more pawls than rotors!");
            }
            holderPerm = _config.next();
            allRotors = new ArrayList<Rotor>();
            while (_config.hasNext()) {
                rotorName = holderPerm;
                typeNotch = _config.next();
                allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, numPawls, allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            String myPermutation = "";
            holderPerm = _config.next();
            while (holderPerm.contains("(") && _config.hasNext()) {
                myPermutation = myPermutation + holderPerm;
                holderPerm = _config.next();
            }
            if (!_config.hasNext()) {
                myPermutation = myPermutation + holderPerm + " ";
            }

            char errorCheck = typeNotch.charAt(0);
            if (errorCheck == 'N' || errorCheck == 'M' || errorCheck == 'R') {
                for (int i = 1; i < typeNotch.length(); i++) {
                    for (int j = i + 1; j < typeNotch.length(); j++) {
                        if (typeNotch.charAt(i) == typeNotch.charAt(j)) {
                            throw error("No duplicate notches please!");
                        }
                    }
                }
            }

            if (typeNotch.charAt(0) == 'N') {
                return new FixedRotor(rotorName,
                        new Permutation(myPermutation, _alphabet));
            }
            if (typeNotch.charAt(0) == 'M') {
                return new MovingRotor(rotorName,
                        new Permutation(myPermutation, _alphabet),
                        typeNotch.substring(1));
            }
            if (typeNotch.charAt(0) == 'R') {
                if (typeNotch.length() > 1) {
                    throw error("Reflectors DO NOT contain notches!");
                }
                return new Reflector(rotorName,
                        new Permutation(myPermutation, _alphabet));
            } else {
                throw error("Reflector type not defined!");
            }

        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {

        String[] splitSet = settings.split(" ");
        if (!splitSet[0].equals("*")) {
            throw new EnigmaException("Not the correct format!");
        }
        String[] rotorNames = new String[M.numRotors()];

        if (rotorNames.length >= 0) {
            System.arraycopy(splitSet, 1, rotorNames, 0, rotorNames.length);
        }
        M.insertRotors(rotorNames);

        for (int i = 0; i < rotorNames.length; i++) {
            for (int j = i + 1; j < rotorNames.length; j++) {
                if (rotorNames[i].equals(rotorNames[j])) {
                    throw error("Cannot repeat rotors!");
                }
            }
        }

        if (!(M._activeRotor[0] instanceof Reflector)) {
            throw error("First rotor must be a reflector!");
        }
        if (splitSet[rotorNames.length + 1].contains("(")
                || splitSet[rotorNames.length + 1].contains(")")) {
            throw error("Cannot have parenthesis in the setting! Try again!");
        }

        M.setRotors(splitSet[rotorNames.length + 1]);

        if (splitSet.length > rotorNames.length + 2) {
            String c = splitSet[rotorNames.length + 2];
            if ((!c.contains("(")) && c.length() == M.numRotors() - 1) {
                for (int i = 0; i < M.numRotors() - 1; i++) {
                    M._activeRotor[i + 1].setRingSetting(c.charAt(i));
                }
            }
        }

        String permuteAdd = "";
        for (int i = splitSet.length - 1; i > 0; i--) {
            if (splitSet[i].contains("(")) {
                permuteAdd = permuteAdd.concat(splitSet[i] + " ");
            }
        }

        permuteAdd = permuteAdd.replace("(", "");
        permuteAdd = permuteAdd.replace(")", "");
        String[] permuteAddArray = permuteAdd.split(" ");
        for (String i: permuteAddArray) {
            if (i.length() > 2) {
                throw error("Plugboard can only have a 2 element mapping");
            }
        }
        M.setPlugboard(new Permutation(permuteAdd, _alphabet));
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        int remainingLetters = msg.length();
        for (int i = 0; i < msg.length(); i += 5) {
            if (remainingLetters > 5) {
                _output.print(msg.substring(i, i + 5) + " ");
                remainingLetters -= 5;
            } else {
                _output.println(msg.substring(i, remainingLetters + i));
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Rotor Name. */
    private String rotorName;

    /** Type of rotor and Notches. */
    private String typeNotch;

    /** Helper variable for permutation. */
    private String holderPerm;

    /** Stores all rotors.*/
    private ArrayList<Rotor> allRotors;
}
