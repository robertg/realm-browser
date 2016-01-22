package de.jonasrottmann.realmbrowser;

import android.content.Context;
import io.realm.Realm;

/**
 * Meshable, Inc. Copyright 2016
 */
public interface RealmFactory {
  public Realm getInstance(Context context);
}
