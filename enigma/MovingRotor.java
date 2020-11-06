package enigma;

import static enigma.EnigmaException.*;

/** Class that represents a rotating rotor in the enigma machine.
 *  @author Pranav Bhimani
 */
class MovingRotor extends Rotor {

    /** A rotor named NAME whose permutation in its default setting is
     *  PERM, and whose notches are at the positions indicated in NOTCHES.
     *  The Rotor is initally in its 0 setting (first character of its
     *  alphabet).
     */
    MovingRotor(String name, Permutation perm, String notches) {
        super(name, perm);
        _notches = notches;
    }

    @Override
    boolean rotates() {
        return true;
    }

    @Override
    void advance() {
        set(permutation().wrap(setting() + 1));
    }

    @Override
    boolean atNotch() {
        String check = Character.toString(alphabet().toChar(setting()));
        return _notches.contains(check);
    }

    /** The specific notches of this moving rotor represented as a String. */
    private String _notches;

}
