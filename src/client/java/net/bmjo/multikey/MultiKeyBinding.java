package net.bmjo.multikey;

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
        boundKeys = keys.length != 0 ? Sets.newHashSet(keys) : Sets.newHashSet(InputUtil.UNKNOWN_KEY);
        defaultKeys = boundKeys;

        KEY_BINDINGS.add(this);
        safeKeyBindings();
    }

    private void safeKeyBindings() {
        for (InputUtil.Key key : this.boundKeys)
            KEY_TO_BINDINGS.computeIfAbsent(key, k -> Sets.newHashSet(this)).add(this);
    }

    @Override
    public void setBoundKey(InputUtil.Key boundKey) {
        this.boundKeys.clear();
        super.setBoundKey(boundKey);
    }

    public void setBoundKeys(Set<InputUtil.Key> boundKeys) {
        if (boundKeys.isEmpty()) {
            this.setBoundKey(InputUtil.UNKNOWN_KEY);
        } else if (boundKeys.size() == 1) {
            this.setBoundKey(boundKeys.iterator().next());
        } else {
            super.setBoundKey(InputUtil.UNKNOWN_KEY);
            this.boundKeys = boundKeys;
        }
    }

    @Override
    public boolean equals(KeyBinding other) {
        return !boundKeys.isEmpty() ? other instanceof MultiKeyBinding multiKeyBinding && this.boundKeys.equals(multiKeyBinding.boundKeys) : super.equals(other);
    }

    public boolean areSubsets(MultiKeyBinding otherKeyBinding) {
        return this.boundKeys.containsAll(otherKeyBinding.boundKeys) || otherKeyBinding.boundKeys.containsAll(this.boundKeys);
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
    public boolean isDefault() {
        return this.boundKeys.equals(this.defaultKeys);
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
                text.append(" + ");
            }
        }
        return text.toString();
    }

    public boolean allPressed() { //TODO auhc wenn einzeln ist
        long window = MinecraftClient.getInstance().getWindow().getHandle();
        return this.defaultKeys.stream().allMatch(k -> InputUtil.isKeyPressed(window, k.getCode()));
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
            KeyBinding.setKeyPressed(key, false);
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
}
