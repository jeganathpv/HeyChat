package com.jaddu.heychat.heychat;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Date;



public class SecondActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseListAdapter<ChatMessage> adapter;
    FloatingActionButton fab;

    DatabaseReference dReference;
    ListView listOfMessage;
    String branch;

    private static final int TIME_DELAY=2000;
    private static long back_pressed;


    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        adapter.startListening();
    }

    @Override
    public void onBackPressed() {
        if(back_pressed+TIME_DELAY > System.currentTimeMillis()){
            Intent intent=new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(this,"Press once again to exit",Toast.LENGTH_SHORT).show();
        }
        back_pressed=System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


        fab=(FloatingActionButton)findViewById(R.id.fab);
        listOfMessage= (ListView)findViewById(R.id.list_of_message);

        dReference=FirebaseDatabase.getInstance().getReference();

        mAuth=FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()==null){
                    startActivity(new Intent(SecondActivity.this,MainActivity.class));
                }
            }
        };

        Date date=new Date();
        SimpleDateFormat formatYear=new SimpleDateFormat("dd-MM-yyyy");
        branch=formatYear.format(date);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input= (EditText)findViewById(R.id.input);
                FirebaseDatabase.getInstance().getReference().child(branch).push().setValue(new ChatMessage(input.getText().toString(),
                        FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                input.setText("");
            }
        });

        displayChatMessage();

    }


    private void displayChatMessage() {

        Query query=dReference.child(branch);
//The error said the constructor expected FirebaseListOptions - here you create them:
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .setLayout(R.layout.list_item)
                .build();


        //Finally you pass them to the constructor here:
        adapter = new FirebaseListAdapter<ChatMessage>(options){
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml

                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);


                String currentUser=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                if(model.getMessageUser().equals(currentUser)){
                    messageUser.setText("You");
//                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)messageText.getLayoutParams();
                    messageText.setBackgroundResource(R.drawable.bubble_user);
//                    params.addRule(RelativeLayout.ALIGN_PARENT_END);
//                    messageText.setLayoutParams(params);

                }else{
                    messageUser.setText(model.getMessageUser());
//                    RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)messageText.getLayoutParams();
//                    params.addRule(RelativeLayout.ALIGN_PARENT_START);
                    messageText.setBackgroundResource(R.drawable.bubble);
//                    messageText.setLayoutParams(params);

                }


                // Set thier user
//                messageUser.setText(model.getMessageUser());
                // Set their text
                messageText.setText(model.getMessageText());
                // Format the date before showing it
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)", model.getMessageTime()));
            }
        };


        listOfMessage.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.logout){
            Toast.makeText(SecondActivity.this,"You have been logged out!",Toast.LENGTH_SHORT).show();
            mAuth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }



}
