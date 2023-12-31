package net.bmjo.multikey;

import com.google.common.collect.Sets;
import net.bmjo.multikey.annotation.Unfinished;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

@Unfinished
public class MultiKeyBinding extends KeyBinding {
    private static final Set<MultiKeyBinding> KEY_BINDINGS = new HashSet<>();
    private static final Map<InputUtil.Key, Set<MultiKeyBinding>> KEY_TO_BINDINGS = new HashMap<>();
    private final Collection<InputUtil.Key> defaultKeys;
    private final Collection<InputUtil.Key> boundKeys;
    private int timesPressed;
    private final boolean sneak;

    public static Set<MultiKeyBinding> getMultiKeyBinding(InputUtil.Key key) {
        return KEY_TO_BINDINGS.containsKey(key) ? KEY_TO_BINDINGS.get(key) : new HashSet<>();
    }

    public static void updatePressedStates() {
        for (MultiKeyBinding multiKeyBinding : KEY_BINDINGS)
            multiKeyBinding.setPressed(multiKeyBinding.allPressed());
    }

    public static void unpressAll() {
        for (MultiKeyBinding multiKeyBinding : KEY_BINDINGS)
            multiKeyBinding.reset();
    }

    public static void updateBindings() {
        KEY_TO_BINDINGS.clear();
        for (MultiKeyBinding multiKeyBinding : KEY_BINDINGS)
            multiKeyBinding.safeKeyBindings();
    }

    public MultiKeyBinding(String translationKey, String category, boolean sneak, InputUtil.Key... keys) {
        super(translationKey, GLFW.GLFW_KEY_UNKNOWN, category);
        this.sneak = sneak;
        this.boundKeys = Sets.newHashSet(keys);
        this.defaultKeys = this.boundKeys;

        KEY_BINDINGS.add(this);
        safeKeyBindings();
    }

    public void safeKeyBindings() {
        for (InputUtil.Key key : this.boundKeys)
            KEY_TO_BINDINGS.computeIfAbsent(key, k -> Sets.newHashSet(this)).add(this);
    }

    @Override
    public boolean wasPressed() {
        if (this.timesPressed == 0) {
            return false;
        } else {
            --this.timesPressed;
            return true;
        }
    }

    public void onPressed() {
        ++this.timesPressed;
    }

    public boolean allPressed() {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;
        long window = mc.getWindow().getHandle();
        if (this.sneak && player != null && !player.isSneaking())
            return false;
        for (InputUtil.Key key : this.boundKeys)
            if (key.getCategory() == InputUtil.Type.MOUSE && GLFW.glfwGetMouseButton(window, key.getCode()) != GLFW.GLFW_PRESS)
                return false;
            else if (key.getCategory() != InputUtil.Type.MOUSE && !InputUtil.isKeyPressed(window, key.getCode()))
                return false;
        return true;
    }

    public void reset() {
        this.timesPressed = 0;
        this.setPressed(false);
    }

    @Override
    public boolean matchesKey(int keyCode, int scanCode) {
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
            return this.boundKeys.stream().anyMatch(key -> key.getCategory() == InputUtil.Type.SCANCODE && key.getCode() == scanCode);
        } else {
            return this.boundKeys.stream().anyMatch(key -> key.getCategory() == InputUtil.Type.KEYSYM && key.getCode() == keyCode);
        }
    }

    @Override
    public boolean matchesMouse(int code) {
        return this.boundKeys.stream().anyMatch(key -> key.getCategory() == InputUtil.Type.MOUSE && key.getCode() == code);
    }

    @Override
    public boolean isDefault() {
        return !this.boundKeys.isEmpty() ? this.boundKeys.equals(this.defaultKeys) : super.isDefault();
    }

    public boolean isUnbound() {
        return this.boundKeys.isEmpty() || super.isUnbound();
    }

    @Override
    public Text getBoundKeyLocalizedText() {
        if (this.boundKeys.isEmpty())
            return super.getBoundKeyLocalizedText();
        StringBuilder text = new StringBuilder();
        Iterator<InputUtil.Key> itr = this.boundKeys.iterator();
        while (itr.hasNext()) {
            InputUtil.Key key = itr.next();
            text.append(key.getLocalizedText().getString());
            if (itr.hasNext()) {
                text.append(" + ");
            }
        }
        return Text.literal(text.toString());
    }

    @Override
    public String getBoundKeyTranslationKey() {
        if (this.boundKeys.isEmpty())
            return super.getBoundKeyTranslationKey();
        StringBuilder text = new StringBuilder();
        Iterator<InputUtil.Key> itr = this.boundKeys.iterator();
        while (itr.hasNext()) {
            InputUtil.Key key = itr.next();
            text.append(key.getTranslationKey());
            if (itr.hasNext()) {
                text.append("-");
            }
        }
        return text.toString();
    }

    @Override
    public boolean equals(KeyBinding other) {
        return !boundKeys.isEmpty() ? other instanceof MultiKeyBinding multiKeyBinding && this.boundKeys.equals(multiKeyBinding.boundKeys) : super.equals(other);
    }
}
