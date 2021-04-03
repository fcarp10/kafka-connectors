package aggregator;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileInputStream;
import java.io.IOException;


class FirebaseAdmin {

   private DatabaseReference databaseReference;

   FirebaseAdmin(String configFile, String firebaseEndpoint, String databaseRef) {
      FirebaseOptions options = null;
      try {
         FileInputStream serviceAccount =
                 new FileInputStream(configFile);
         options = new FirebaseOptions.Builder()
                 .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                 .setDatabaseUrl(firebaseEndpoint)
                 .build();

      } catch (IOException e) {
         e.printStackTrace();
      }
      FirebaseApp.initializeApp(options);
      final FirebaseDatabase database = FirebaseDatabase.getInstance();
      databaseReference = database.getReference(databaseRef);
   }

   void saveData(String path, String id, Object object) {
      DatabaseReference childRef = databaseReference.child(path);
      childRef.child(id).setValueAsync(object);
   }
}
