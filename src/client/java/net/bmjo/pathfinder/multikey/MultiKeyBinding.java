package net.bmjo.pathfinder.multikey;

import com.google.common.collect.Sets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class MultiKeyBinding extends KeyBinding {
    private static final Set<MultiKeyBinding> KEY_BINDINGS = new HashSet<>();
    private static final Map<InputUtil.Key, Set<MultiKeyBinding>> KEY_TO_BINDINGS = new HashMap<>();
    private final Collection<InputUtil.Key> defaultKeys;
    private Collection<InputUtil.Key> boundKeys;
    int timesPressed;

    public MultiKeyBinding(String translationKey, String category, InputUtil.Key... keys) {
        super(translationKey, GLFW.GLFW_KEY_UNKNOWN, category);
        boundKeys = Set.of(keys);
        defaultKeys = boundKeys;

        KEY_BINDINGS.add(this);
        safeKeyBindings();
    }

    private void safeKeyBindings() {
        for (InputUtil.Key key : boundKeys)
            KEY_TO_BINDINGS.computeIfAbsent(key, k -> Sets.newHashSet(this)).add(this);
    }


    public void setBoundKeys(Set<InputUtil.Key> boundKey) {
        if (boundKey.isEmpty())
            return;
        this.boundKeys = boundKey;
    }

    @Override
    public boolean equals(KeyBinding other) {
        return other instanceof MultiKeyBinding multiKeyBinding && this.boundKeys.equals(multiKeyBinding.boundKeys);
    }

    public boolean isUnbound() {
        return this.boundKeys.isEmpty();
    }

    @Override
    public boolean matchesKey(int keyCode, int scanCode) {
        if (keyCode == InputUtil.UNKNOWN_KEY.getCode()) {
            return this.boundKeys.stream().allMatch(key -> key.getCategory() == InputUtil.Type.SCANCODE && key.getCode() == scanCode);
        } else {
            return this.boundKeys.stream().allMatch(key -> key.getCategory() == InputUtil.Type.KEYSYM && key.getCode() == keyCode);
        }
    }

    @Override
    public boolean matchesMouse(int code) {
        return this.boundKeys.stream().allMatch(key -> key.getCategory() == InputUtil.Type.MOUSE && key.getCode() == code);
    }

    @Override
    public Text getBoundKeyLocalizedText() {
        StringBuilder text = new StringBuilder();
        for (InputUtil.Key key : this.boundKeys)
            text.append(key.getLocalizedText()).append(" + ");
        return Text.literal(text.toString());
    }

    @Override
    public boolean isDefault() {
        return this.boundKeys.equals(this.defaultKeys);
    }

    @Override
    public String getBoundKeyTranslationKey() {
        return "";
    }

    public boolean allPressed() {
        long handle = MinecraftClient.getInstance().getWindow().getHandle();
        return this.defaultKeys.stream().allMatch(k -> InputUtil.isKeyPressed(handle, k.getCode()));
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

    void reset() {
        this.timesPressed = 0;
        this.setPressed(false);
    }

    public void disableOtherBindings() {
        for (InputUtil.Key key : this.boundKeys)
            KeyBinding.setKeyPressed(key, false); // OR RESET
    }

    public static Set<MultiKeyBinding> getMultiKeyBinding() {
        return KEY_BINDINGS;
    }

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
    //TODO are same teilmenge
}
