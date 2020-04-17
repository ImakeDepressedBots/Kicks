package com.diablo.jayson.kicksv1.UI.AttendActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.diablo.jayson.kicksv1.ApiThings;
import com.diablo.jayson.kicksv1.Models.Activity;
import com.diablo.jayson.kicksv1.Models.AttendingUser;
import com.diablo.jayson.kicksv1.Models.ChatItem;
import com.diablo.jayson.kicksv1.R;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

public class MainAttendActivityActivity extends AppCompatActivity implements OnMapReadyCallback {

    private ChatAdapter chatAdapter;

    private RelativeLayout dashItemsRelativeLayout;
    private RelativeLayout chatActualRelativeLayout;
    private RelativeLayout attendeesActualRelativeLayout;
    private RelativeLayout detailsActualRelativeLayout;
    private FrameLayout dashItemDetailsFramelayout;
    private RecyclerView attendeesRecycler, attendeesActualRecycler, chatRecycler, chatActualRecycler;
    private ProgressBar attendeesProress;
    private ArrayList<AttendingUser> attendingUsersData;
    private CardView chatCard;
    private ImageView activityImageView;
    private RelativeLayout chatCardOverlay;
    private RelativeLayout attendeesCardOverlay;
    private RelativeLayout detailsCardOverlay;
    private ImageView sendMessageButton;
    private EditText messageEdit;
    private TextView activityDashTimeText, activityDashDateText, activityDashLocationText, activityDashTagText;
    private TextView activityLocationActualTextView, activityTimeActualTextView;

    private LatLng activityLocation;
    private GoogleMap googleMap;
    private String activityTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_attend_activity);
        attendeesRecycler = findViewById(R.id.attendeesRecycler);
        attendeesActualRecycler = findViewById(R.id.attendeesActualRecycler);
        chatRecycler = findViewById(R.id.chatRecycler);
        chatActualRecycler = findViewById(R.id.chatActualRecycler);
        dashItemsRelativeLayout = findViewById(R.id.dash_items_relative_Layout);
        chatActualRelativeLayout = findViewById(R.id.chatActualRelativeLayout);
        attendeesActualRelativeLayout = findViewById(R.id.attendeesActualRelativeLayout);
        detailsActualRelativeLayout = findViewById(R.id.activityDetailsActualRelativeLayout);
//        dashItemDetailsFramelayout = findViewById(R.id.dashItems_fragment_container);
//        dashItemDetailsFramelayout.setVisibility(View.GONE);
        chatCardOverlay = findViewById(R.id.chatCardOverlay);
        attendeesCardOverlay = findViewById(R.id.attendeesCardOverlay);
        detailsCardOverlay = findViewById(R.id.detailsCardOverlay);
        chatCard = findViewById(R.id.activityChatCard);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEdit = findViewById(R.id.messageEditText);
        activityDashTimeText = findViewById(R.id.activityDashTimeText);
        activityDashDateText = findViewById(R.id.activityDashDateText);
        activityDashLocationText = findViewById(R.id.activityDashLocationText);
        activityDashTagText = findViewById(R.id.activityDashTagTextView);
        activityImageView = findViewById(R.id.activityImageView);
        activityLocationActualTextView = findViewById(R.id.activity_actual_location_text_view);
        activityTimeActualTextView = findViewById(R.id.activity_time_actual_text_view);

        Bundle bundle = getIntent().getExtras();

        assert bundle != null;
        String activityId = bundle.getString("activityId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Places.initialize(this, ApiThings.places_api_key);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getSupportFragmentManager()
                .findFragmentById(R.id.activity_map_location);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.attend_activity_toolbar);
        setSupportActionBar(myToolbar);


        assert activityId != null;
        db.collection("activities").document(activityId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    assert documentSnapshot != null;
                    String activityTime = documentSnapshot.toObject(Activity.class).getKickStartTime() + " - " + documentSnapshot.toObject(Activity.class).getKickEndTime();
                    String activityDate = documentSnapshot.toObject(Activity.class).getKickDate();
                    String activityImageUrl = documentSnapshot.toObject(Activity.class).getImageUrl();
                    String activityTitle = documentSnapshot.toObject(Activity.class).getKickTitle();
                    String activityLocationName = documentSnapshot.toObject(Activity.class).getKickLocationName();
                    String activityTag = documentSnapshot.toObject(Activity.class).getTag().getTagName();
                    activityLocation = new LatLng(documentSnapshot.toObject(Activity.class).getKickLocationCordinates().getLatitude(),
                            documentSnapshot.toObject(Activity.class).getKickLocationCordinates().getLongitude());
                    activityDashTimeText.setText(activityTime);
                    activityDashLocationText.setText(activityLocationName);
                    activityDashTagText.setText(activityTag);
                    activityDashDateText.setText(activityDate);
                    activityLocationActualTextView.setText(activityLocationName);
                    activityTimeActualTextView.setText(activityTime);
                    Glide.with(getApplicationContext())
                            .load(activityImageUrl)
                            .into(activityImageView);
                    Objects.requireNonNull(getSupportActionBar()).setTitle(activityTitle);
                    googleMap.clear();
                    googleMap.addMarker(new MarkerOptions().position(activityLocation)
                            .title(activityTitle));
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(activityLocation, 15));

                    attendingUsersData = new ArrayList<AttendingUser>();
                    attendingUsersData = Objects.requireNonNull(documentSnapshot.toObject(Activity.class)).getMattendees();
//                            Log.e("yooooo", String.valueOf(attendingUsersData.size()));
//                            Log.e("yooooo", attendingUsersData.get(0).getUserName());
//                            Log.e("yooooo", attendingUsersData.get(1).getUserName());
//                            Log.e("yooooo", attendingUsersData.get(2).getUserName());
//                            Log.e("yooooo", attendingUsersData.get(3).getUserName());


                }
//                        initializeRecyclerWithAttendees();
//                        Log.e("skkdnskn", attendingUsersData.get(3).getUserName());
                AttendeesAdapter attendeesAdapter = new AttendeesAdapter(MainAttendActivityActivity.this, attendingUsersData);
                attendeesRecycler.setLayoutManager(new GridLayoutManager(MainAttendActivityActivity.this, 2, GridLayoutManager.HORIZONTAL, false));
                attendeesRecycler.setAdapter(attendeesAdapter);
                AttendeesLargeAdapter attendeesLargeAdapter = new AttendeesLargeAdapter(MainAttendActivityActivity.this, attendingUsersData);
                attendeesActualRecycler.setLayoutManager(new GridLayoutManager(MainAttendActivityActivity.this, 2, GridLayoutManager.VERTICAL, false));
                attendeesActualRecycler.setAdapter(attendeesLargeAdapter);
            }
        });

        Query query = FirebaseFirestore.getInstance()
                .collection("activities")
                .document(activityId)
                .collection("chatsession")
                .orderBy("timestamp", Query.Direction.ASCENDING);


        FirestoreRecyclerOptions<ChatItem> options = new FirestoreRecyclerOptions.Builder<ChatItem>()
                .setQuery(query, ChatItem.class)
                .build();
        chatAdapter = new ChatAdapter(options, getApplicationContext());
        int gridColumnCount = 1;
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        chatRecycler.setLayoutManager(layoutManager);

//                layoutManager.setStackFromEnd(true);
//                layoutManager.setReverseLayout(true);


        chatRecycler.post(new Runnable() {
            @Override
            public void run() {
                new CountDownTimer(Integer.MAX_VALUE, 20) {
                    public void onTick(long millis) {
                        chatRecycler.scrollBy(0, 1);
                    }

                    @Override
                    public void onFinish() {

                    }
                }.start();
            }
        });
        chatRecycler.setAdapter(chatAdapter);
//                chatAdapter.notifyDataSetChanged();
        LinearLayoutManager chatActuallayoutManager = new LinearLayoutManager(this);

        chatActualRecycler.setLayoutManager(chatActuallayoutManager);
        chatActualRecycler.setAdapter(chatAdapter);
        chatAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                chatActualRecycler.scrollToPosition(chatAdapter.getItemCount() - 1);
            }
        });

        chatAdapter.startListening();

        chatCardOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatActualRelativeLayout.setVisibility(View.VISIBLE);
                dashItemsRelativeLayout.setVisibility(View.GONE);
            }
        });
        attendeesCardOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendeesActualRelativeLayout.setVisibility(View.VISIBLE);
                dashItemsRelativeLayout.setVisibility(View.GONE);
            }
        });
        detailsCardOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsActualRelativeLayout.setVisibility(View.VISIBLE);
                dashItemsRelativeLayout.setVisibility(View.GONE);
            }
        });


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String message = messageEdit.getText().toString();
                if (!message.isEmpty()) {
                    ChatItem messageItem = new ChatItem();
                    messageItem.setMessage(message);
                    assert user != null;
                    messageItem.setSenderName(user.getDisplayName());
                    messageItem.setSenderPicUrl(Objects.requireNonNull(user.getPhotoUrl()).toString());
                    messageItem.setSenderUid(user.getUid());
                    messageItem.setSender(true);
                    messageItem.setTimestamp(Timestamp.now());

                    FirebaseFirestore.getInstance().collection("activities").document(activityId)
                            .collection("chatsession")
                            .add(messageItem)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.e("Yellow", "Added Mesage");
                                    messageEdit.setText("");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Try Sending Message Again", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });


    }

    @Override
    public void onBackPressed() {

        if (chatActualRelativeLayout.getVisibility() == View.VISIBLE) {
            chatActualRelativeLayout.setVisibility(View.GONE);
            dashItemsRelativeLayout.setVisibility(View.VISIBLE);
        } else if (attendeesActualRelativeLayout.getVisibility() == View.VISIBLE) {

            attendeesActualRelativeLayout.setVisibility(View.GONE);
            dashItemsRelativeLayout.setVisibility(View.VISIBLE);
        } else if (detailsActualRelativeLayout.getVisibility() == View.VISIBLE) {
            detailsActualRelativeLayout.setVisibility(View.GONE);
            dashItemsRelativeLayout.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
//

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0))
                .title(activityTitle));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 15));
    }
}
