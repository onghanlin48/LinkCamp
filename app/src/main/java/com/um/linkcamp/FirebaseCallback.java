package com.um.linkcamp;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface FirebaseCallback {
        void onCallback(boolean Exists) throws IOException, NoSuchAlgorithmException;
}
