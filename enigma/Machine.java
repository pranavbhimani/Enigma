package enigma;


import java.util.ArrayList;
import java.util.Collection;
import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Pranav Bhimani
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _numRotors = numRotors;
        _pawls = pawls;
        _allRotors = allRotors;
        _activeRotor = new Rotor[_numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _numRotors;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        if (rotors.length != _numRotors) {
            throw new EnigmaException("Sizes are not equal!");
        }
        for (int i = 0; i < rotors.length; i++) {
            for (int j = 0; j < _allRotors.size(); j++) {
                Rotor check = (Rotor) (_allRotors.toArray()[j]);
                if (rotors[i].equals(check.name())) {
                    _activeRotor[i] = check;
                }
            }
        }

        if (!(_activeRotor[_activeRotor.length - 1] instanceof MovingRotor)) {
            throw error("Final rotor must be a moving rotor!");
        }

        if (!(_activeRotor[0] instanceof Reflector)) {
            throw error("First rotor must be a reflector!");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        for (int i = 0; i < setting.length(); i++) {
            if (!_alphabet.contains(setting.charAt(i))) {
                throw error("Settings must contain characters from alphabet!");
            }
        }
        for (int i = 0; i < setting.length(); i++) {
            _activeRotor[i + 1].set(setting.charAt(i));
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        int plugChange = _plugboard.permute(c);

        ArrayList<Integer> advanceArray = new ArrayList<Integer>();
        for (int i = _activeRotor.length - 2; i > 0; i--) {
            if (((_activeRotor[i].atNotch())
                    && _activeRotor[i - 1].rotates())
                    || _activeRotor[i + 1].atNotch()) {
                advanceArray.add(i);
            }
        }

        for (int i = 0; i < advanceArray.size(); i++) {
            _activeRotor[advanceArray.get(i)].advance();
        }

        _activeRotor[_activeRotor.length - 1].advance();

        for (int i = _activeRotor.length - 1; i >= 0; i--) {
            plugChange = _activeRotor[i].convertForward(plugChange);
        }
        for (int i = 1; i < _activeRotor.length; i++) {
            plugChange = _activeRotor[i].convertBackward(plugChange);
        }

        plugChange = _plugboard.permute(plugChange);

        return plugChange;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        char[] message = msg.toCharArray();
        for (int i = 0; i < message.length; i++) {
            message[i] = _alphabet.toChar(convert(_alphabet.toInt(message[i])));
        }
        return new String(message);
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotors. */
    private int _numRotors;

    /** Number of pawls. */
    private int _pawls;

    /** All possible rotors in a collection. */
    private Collection<Rotor> _allRotors;

    /** Rotors in use stored in an array. */
    protected Rotor[] _activeRotor;

    /** permutation of the plugboard. */
    private Permutation _plugboard;
}
