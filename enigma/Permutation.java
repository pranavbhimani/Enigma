package enigma;

import java.util.HashMap;


/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Pranav Bhimani
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        perm = new HashMap<Character, Character>();
        _cycles = cycles;
        splitted = _cycles.replace("(", " ").replace(")", " ").split(" ");
        for (int i = 0; i < splitted.length; i++) {
            addCycle(splitted[i]);
        }

        for (int i = 0; i < alphabet().size(); i++) {
            if (!perm.containsKey(_alphabet.toChar(i))) {
                perm.put(_alphabet.toChar(i), alphabet().toChar(i));
            }
        }

    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        for (int j = 0; j < cycle.length(); j++) {
            if (j == cycle.length() - 1) {
                perm.put(cycle.charAt(j), cycle.charAt(0));
            } else {
                perm.put(cycle.charAt(j), cycle.charAt(j + 1));
            }
        }
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return alphabet().size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        int val = wrap(p);
        char check = alphabet().toChar(val);
        char changed = permute(check);
        return alphabet().toInt(changed);
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        int val = wrap(c);
        char check = alphabet().toChar(val);
        char changed = invert(check);
        return alphabet().toInt(changed);
    }

    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        if (!_alphabet.contains(p)) {
            throw new EnigmaException("Alphabet not contain character!");
        }
        return perm.get(p);
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        if (!_alphabet.contains(c)) {
            throw new EnigmaException("Alphabet not contain character!");
        }
        for (char i : perm.keySet()) {
            if (perm.get(i) == c) {
                return i;
            }
        }
        return c;
    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        for (char i : perm.keySet()) {
            if (perm.get(i) == i) {
                return false;
            }
        }
        return true;
    }

    /** Alphabet of this permutation. */
    protected Alphabet _alphabet;

    /** The permutation represented in a HashMap. */
    private HashMap<Character, Character> perm;

    /** Each cycle represented in an array. */
    private String[] splitted;

    /** Each cycle of this permutation. */
    protected String _cycles;
}
