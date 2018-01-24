package de.tum.ase.kleo.app.support.ui;

import android.widget.Spinner;

import java.util.Optional;

public class ArrayAdapterItem<T> {

    private String label;
    private T value;

    private ArrayAdapterItem(String label, T value) {
        this.label = label;
        this.value = value;
    }

    public static <I> ArrayAdapterItem<I> of(String label, I value) {
        return new ArrayAdapterItem<>(label, value);
    }

    @SuppressWarnings("unchecked")
    public static <I> Optional<I> getSelectedItemValue(Spinner spinner, Class<I> type) {
        final Object selectedItem = spinner.getSelectedItem();

        if (selectedItem == null)
            return Optional.empty();

        if (!ArrayAdapterItem.class.isAssignableFrom(selectedItem.getClass()))
            throw new IllegalArgumentException("Item is not of ArrayAdapterItem type");

        final ArrayAdapterItem<I> selectedItemCasted = (ArrayAdapterItem<I>) selectedItem;

        return Optional.of(selectedItemCasted.value());
    }

    public T value() {
        return value;
    }

    @Override
    public String toString() {
        return label;
    }
}
