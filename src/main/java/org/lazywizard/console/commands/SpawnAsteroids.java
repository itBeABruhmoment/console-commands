package org.lazywizard.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.opengl.DrawUtils;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lazywizard.lazylib.ui.LazyFont.DrawableString;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

import static org.lazywizard.lazylib.opengl.ColorUtils.glColor;
import static org.lwjgl.opengl.GL11.*;

public class SpawnAsteroids implements BaseCommand
{
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if (!context.isInCombat())
        {
            Console.showMessage(CommonStrings.ERROR_COMBAT_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        final CombatEngineAPI engine = Global.getCombatEngine();
        if (engine.getCombatUI().isShowingCommandUI())
        {
            Console.showMessage("Error: this command can't be used while the map screen is open!");
            return CommandResult.ERROR;
        }

        // TODO: Have size selectable within the combat plugin (number keys? +/-?)
        final int size;
        switch (args.toLowerCase())
        {
            case "": // No argument, random size
                size = -1;
                break;
            case "0":
            case "tiny":
                size = 0;
                break;
            case "1":
            case "small":
                size = 1;
                break;
            case "2":
            case "medium":
                size = 2;
                break;
            case "3":
            case "large":
                size = 3;
                break;
            default:
                Console.showMessage("Valid asteroid sizes: tiny, small, medium, large.");
                return CommandResult.BAD_SYNTAX;
        }

        // TODO: Ensure only one plugin exists at a time
        engine.addPlugin(new SpawnPlugin(size));
        Console.showMessage("Click and drag to spawn asteroids, press space to finish spawning.");
        return CommandResult.SUCCESS;
    }

    private static class SpawnPlugin extends BaseEveryFrameCombatPlugin
    {
        private final DrawableString text;
        private final Vector2f spawnLoc = new Vector2f(0f, 0f);
        private final boolean wasPaused;
        private int asteroidSize;
        private boolean mouseDown = false;

        private SpawnPlugin(int asteroidSize)
        {
            this.asteroidSize = asteroidSize;
            wasPaused = Global.getCombatEngine().isPaused();
            final LazyFont font = Console.getFont();
            text = font.createText("Click and drag to spawn asteroids, press space to finish spawning.",
                    Console.getSettings().getOutputColor());
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events)
        {
            final CombatEngineAPI engine = Global.getCombatEngine();
            if (engine.getCombatUI().isShowingCommandUI())
            {
                engine.getCombatUI().addMessage(0, Console.getSettings().getOutputColor(),
                        "Finished spawning asteroids.");
                engine.removePlugin(this);
                return;
            }

            final ViewportAPI view = engine.getViewport();
            final Vector2f mouseLoc = new Vector2f(view.convertScreenXToWorldX(Mouse.getX()),
                    view.convertScreenYToWorldY(Mouse.getY()));
            for (InputEventAPI event : events)
            {
                if (event.isConsumed())
                {
                    continue;
                }

                if (event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_SPACE)
                {
                    event.consume();
                    engine.getCombatUI().addMessage(0, Console.getSettings().getOutputColor(),
                            "Finished spawning asteroids.");
                    engine.removePlugin(this);
                    engine.setPaused(wasPaused);
                    return;
                }
                else if (event.isMouseDownEvent() && event.getEventValue() == 0)
                {
                    if (!mouseDown)
                    {
                        mouseDown = true;
                        spawnLoc.set(mouseLoc);
                    }

                    event.consume();
                }
                else if (event.isMouseUpEvent() && event.getEventValue() == 0)
                {
                    if (mouseDown)
                    {
                        mouseDown = false;
                        final Vector2f velocity = Vector2f.sub(mouseLoc, spawnLoc, null);
                        final int size = (asteroidSize < 0 ? (int) (Math.random() * 4) : asteroidSize);
                        engine.spawnAsteroid(size, spawnLoc.x, spawnLoc.y, velocity.x, velocity.y);
                    }

                    event.consume();
                }
            }

            // Engine must be paused to work around mouse event consumption bug
            engine.setPaused(true);
        }

        private static float getAsteroidSize(int sizeCategory)
        {
            switch (sizeCategory)
            {
                case 0:
                    return 5f;
                case 1:
                    return 10f;
                case 2:
                    return 18f;
                case 3:
                    return 26f;
                default:
                    return 10f;
            }
        }

        @Override
        public void renderInWorldCoords(ViewportAPI view)
        {
            final Vector2f mouseLoc = new Vector2f(view.convertScreenXToWorldX(Mouse.getX()),
                    view.convertScreenYToWorldY(Mouse.getY()));
            final float circleSize = getAsteroidSize(asteroidSize);

            glEnable(GL_BLEND);
            glEnable(GL_TEXTURE_2D);
            text.draw(mouseLoc.x - (text.getWidth() / 2f), mouseLoc.y - 50f);
            glDisable(GL_TEXTURE_2D);
            glLineWidth(5f);
            glColor(Console.getSettings().getOutputColor());

            // If the mouse button is held down, draw the velocity of the asteroid to be spawned
            // Velocity is capped at 600, so draw anything beyond that darkened
            if (mouseDown)
            {
                DrawUtils.drawCircle(spawnLoc.x, spawnLoc.y, circleSize, 32, false);

                if (spawnLoc.equals(mouseLoc)) return;

                final Vector2f drawLoc;
                final boolean beyondRange;
                if (MathUtils.isWithinRange(spawnLoc, mouseLoc, 600))
                {
                    drawLoc = mouseLoc;
                    beyondRange = false;
                }
                else
                {
                    drawLoc = MathUtils.getPointOnCircumference(spawnLoc, 600f,
                            VectorUtils.getAngle(spawnLoc, mouseLoc));
                    beyondRange = true;
                }

                glBegin(GL_LINES);
                glVertex2f(spawnLoc.x, spawnLoc.y);
                glVertex2f(drawLoc.x, drawLoc.y);
                if (beyondRange)
                {
                    glColor(Console.getSettings().getOutputColor(), 0.3f, false);
                    glVertex2f(drawLoc.x, drawLoc.y);
                    glVertex2f(mouseLoc.x, mouseLoc.y);
                }
                glEnd();
            }
            // If the mouse button is not held down, draw a circle around the mouse cursor to show this command is active
            else
            {
                DrawUtils.drawCircle(mouseLoc.x, mouseLoc.y, circleSize, 32, false);
            }
        }
    }
}
