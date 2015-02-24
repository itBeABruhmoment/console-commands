package org.lazywizard.console;

import java.awt.Color;
import java.util.Map;
import org.lazywizard.console.BaseCommand.CommandResult;
import org.lwjgl.input.Keyboard;

/**
 * Contains the console settings loaded from the JSON file.
 *
 * @author LazyWizard
 * @since 2.0
 */
public class ConsoleSettings
{
    // The key stroke that summons the console pop-up
    private final KeyStroke CONSOLE_SUMMON_KEY;
    // The String (usually a single character) that separates multiple commands
    private final String COMMAND_SEPARATOR;
    // The sound ID to be played when a certain CommandResult is returned
    private final Map<CommandResult, String> RESULT_SOUNDS;
    // Whether each command should be displayed to the player before executing
    private final boolean SHOW_ENTERED_COMMANDS;
    // Whether the current cursor index is displayed in the campaign pop-up
    private final boolean SHOW_CURSOR_INDEX;
    // How similar two strings must be for typo correction to decide they match
    private final double TYPO_CORRECTION_THRESHOLD;
    // The color of the console's output text
    private final Color OUTPUT_COLOR;
    // How many characters before the output is line-wrapped
    private final int OUTPUT_MAX_LINE_LENGTH;

    ConsoleSettings(int consoleSummonKey, boolean requireShift, boolean requireControl,
            boolean requireAlt, String commandSeparator, boolean showEnteredCommands,
            boolean showCursorIndex, double typoCorrectionThreshold, Color outputColor,
            int outputMaxLineLength, Map<CommandResult, String> resultSounds)
    {
        CONSOLE_SUMMON_KEY = new KeyStroke(consoleSummonKey, requireShift,
                requireControl, requireAlt);
        COMMAND_SEPARATOR = commandSeparator;
        SHOW_ENTERED_COMMANDS = showEnteredCommands;
        SHOW_CURSOR_INDEX = showCursorIndex;
        TYPO_CORRECTION_THRESHOLD = typoCorrectionThreshold;
        OUTPUT_COLOR = outputColor;
        OUTPUT_MAX_LINE_LENGTH = outputMaxLineLength;
        RESULT_SOUNDS = resultSounds;
    }

    /**
     * Returns the key (and any modifier keys) that must be pressed to summon
     * the console.
     * <p>
     * @return The {@link KeyStroke} to summon the console.
     * <p>
     * @since 2.0
     */
    public KeyStroke getConsoleSummonKey()
    {
        return CONSOLE_SUMMON_KEY;
    }

    /**
     * Returns what character sequence the player must separate multiple
     * commands with.
     * <p>
     * @return The {@link String} (usually a single character) that separates
     *         multiple commands.
     * <p>
     * @since 2.0
     */
    public String getCommandSeparator()
    {
        return COMMAND_SEPARATOR;
    }

    /**
     * Returns whether each valid individual command should be displayed before
     * being run.
     * <p>
     * @return {@code true} if each command should be displayed to the player
     *         before executing, {@code false} otherwise.
     * <p>
     * @since 2.0
     */
    public boolean getShouldShowEnteredCommands()
    {
        return SHOW_ENTERED_COMMANDS;
    }

    public boolean getShouldShowCursorIndex()
    {
        return SHOW_CURSOR_INDEX;
    }

    /**
     * Returns the threshold for similarity between two strings before one is
     * considered a typo of the other.
     * <p>
     * @return How similar two {@link String}s must be for typo correction to
     *         consider them a match.
     * <p>
     * @since 2.2
     */
    public double getTypoCorrectionThreshold()
    {
        return TYPO_CORRECTION_THRESHOLD;
    }

    /**
     * Returns the {@link Color} of the console's output.
     * <p>
     * @return The {@link Color} of the console's output.
     * <p>
     * @since 2.0
     */
    public Color getOutputColor()
    {
        return OUTPUT_COLOR;
    }

    /**
     * Returns how many characters a line of output can reach before it is
     * wrapped.
     * <p>
     * @return How many characters before the output is line-wrapped
     * <p>
     * @since 2.0
     */
    public int getMaxOutputLineLength()
    {
        return OUTPUT_MAX_LINE_LENGTH;
    }

    /**
     * Returns what sound will be played when a command returns a specific
     * result.
     * <p>
     * @param result The {@link CommandResult} to get the sound for.
     * <p>
     * @return The ID of the sound that will be played if a command returns that
     *         {@link CommandResult}, or {@code null} if no sound is set up for
     *         that result.
     * <p>
     * @since 2.0
     */
    public String getSoundForResult(CommandResult result)
    {
        return RESULT_SOUNDS.get(result);
    }

    /**
     * Represents the keys that must be pressed to summon the console.
     * <p>
     * @since 2.0
     */
    public static class KeyStroke
    {
        private final int key;
        private final boolean requireShift;
        private final boolean requireControl;
        private final boolean requireAlt;

        private KeyStroke(int key, boolean requireShift, boolean requireControl,
                boolean requireAlt)
        {
            this.key = key;
            this.requireShift = requireShift;
            this.requireControl = requireControl;
            this.requireAlt = requireAlt;
        }

        /**
         * Returns the key that must be pressed to summon the console.
         * <p>
         * The list of valid key codes can be found here:
         * <a
         * href=http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_0>
         * http://www.lwjgl.org/javadoc/constant-values.html#org.lwjgl.input.Keyboard.KEY_0</a>
         * <p>
         * @return The LWJGL constant of the key that summons the console.
         * <p>
         * @since 2.0
         */
        public int getKey()
        {
            return key;
        }

        /**
         * Returns whether shift must be held down to summon the console.
         * <p>
         * @return Whether you must hold down shift while summoning the console.
         * <p>
         * @since 2.0
         */
        public boolean requiresShift()
        {
            return requireShift;
        }

        /**
         * Returns whether control must be held down to summon the console.
         * <p>
         * @return Whether you must hold down control while summoning the
         *         console.
         * <p>
         * @since 2.0
         */
        public boolean requiresControl()
        {
            return requireControl;
        }

        /**
         * Returns whether alt must be held down to summon the console.
         * <p>
         * @return Whether you must hold down alt while summoning the console.
         * <p>
         * @since 2.0
         */
        public boolean requiresAlt()
        {
            return requireAlt;
        }

        @Override
        public String toString()
        {
            String str = Keyboard.getKeyName(key).toUpperCase();

            if (requireShift)
            {
                str = "SHIFT+" + str;
            }

            if (requireAlt)
            {
                str = "ALT+" + str;
            }

            if (requireControl)
            {
                str = "CONTROL+" + str;
            }

            return str;
        }
    }

    @Override
    public String toString()
    {
        return "ConsoleSettings{" + "CONSOLE_SUMMON_KEY=" + CONSOLE_SUMMON_KEY
                + ", COMMAND_SEPARATOR=" + COMMAND_SEPARATOR
                + ", RESULT_SOUNDS=" + RESULT_SOUNDS
                + ", SHOW_ENTERED_COMMANDS=" + SHOW_ENTERED_COMMANDS
                + ", SHOW_CURSOR_INDEX=" + SHOW_CURSOR_INDEX
                + ", TYPO_CORRECTION_THRESHOLD=" + TYPO_CORRECTION_THRESHOLD
                + ", OUTPUT_COLOR=" + OUTPUT_COLOR
                + ", OUTPUT_MAX_LINE_LENGTH=" + OUTPUT_MAX_LINE_LENGTH + '}';
    }
}
