package function;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;

public class DecimalDigitsInputFilter implements InputFilter {
    private final int decimalDigits;

    public DecimalDigitsInputFilter(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String destText = dest.toString();
        String newText = destText.substring(0, dstart) + source + destText.substring(dend);

        if (newText.contains(".")) {
            String[] parts = newText.split("\\.");
            if (parts.length > 1 && parts[1].length() > decimalDigits) {
                return "";
            }
        }

        return null; // Accept input
    }
}

