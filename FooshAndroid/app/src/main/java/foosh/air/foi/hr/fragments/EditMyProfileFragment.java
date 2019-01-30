package foosh.air.foi.hr.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import foosh.air.foi.hr.R;
import foosh.air.foi.hr.model.User;

public class EditMyProfileFragment extends Fragment {
    private ScrollView contentLayout;
    private User user;
    private int PICK_PHOTO_FOR_AVATAR = 100;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private Context context;
    private TextInputEditText displayName;
    private TextInputEditText city;
    private TextInputEditText bio;
    private TextInputEditText phoneNumber;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        user = new User();
        user.setDisplayName(getArguments().getString("DisplayName"));
        user.setLocation(getArguments().getString("Location"));
        user.setBio(getArguments().getString("Bio"));
        user.setProfileImgPath(getArguments().getString("ProfileImgPath"));
        user.setContact(getArguments().getString("contact"));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = container.getContext();

        return inflater.inflate(R.layout.fragment_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ScrollView scrollView = (ScrollView) view;
        contentLayout = scrollView;

        CircleImageView profilePhoto =  scrollView.findViewById(R.id.userProfileImage);
        displayName = scrollView.findViewById(R.id.userDisplayName);
        city = scrollView.findViewById(R.id.userCity);
        bio = scrollView.findViewById(R.id.userDescription);
        phoneNumber = scrollView.findViewById(R.id.userPhoneNumber);

        if(user.getProfileImgPath() == null || user.getProfileImgPath().equals("")){
            Picasso.get().load(R.drawable.avatar).placeholder(R.drawable.avatar).error(R.drawable.ic_launcher_foreground).into(profilePhoto);
        }else{
            Picasso.get().load(user.getProfileImgPath()).placeholder(R.drawable.avatar).error(R.drawable.ic_launcher_foreground).into(profilePhoto);
        }
        displayName.setText(user.getDisplayName());
        bio.setText(user.getBio());
        if(user.getLocation() != null){
            city.setText(user.getLocation());
        }
        if(user.getContact() != null){
            phoneNumber.setText(user.getContact());
        }

        TextView changePhotoLink = scrollView.findViewById(R.id.linkChangeProfilePhoto);
        changePhotoLink.setOnClickListener(
            new View.OnClickListener()
                   {
                       @Override
                       public void onClick(View v)
                       {
                           openPhotoChoice();
                       }
                   }        );
        Button saveChanges = scrollView.findViewById(R.id.buttonSaveProfile);
        saveChanges.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        saveProfileChanges();
                        FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction mFragmentManagerTransaction = mFragmentManager.beginTransaction();
                        mFragmentManagerTransaction.setCustomAnimations(R.anim.fui_slide_out_left,R.anim.fui_slide_out_left);
                        mFragmentManagerTransaction.remove(mFragmentManager.findFragmentByTag("MY_PROFILE"));
                        mFragmentManagerTransaction.commit();
                        mFragmentManager.popBackStackImmediate();
                    }
                }
        );

        Button getPhoneNumber = scrollView.findViewById(R.id.buttonAddMyNumber);
        getPhoneNumber.setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.READ_PHONE_STATE ) == PackageManager.PERMISSION_GRANTED ){
                            TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
                            String mPhoneNumber = tMgr.getLine1Number();

                            if(mPhoneNumber.equals("")){
                                mPhoneNumber = mAuth.getCurrentUser().getPhoneNumber();

                            }
                            user.setContact(mPhoneNumber);
                            phoneNumber.setText(mPhoneNumber);
                        } else {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {Manifest.permission.READ_PHONE_STATE},
                                    1);
                        }
                    }
                }
        );

    }

    private void saveProfileChanges(){

        user.setDisplayName(displayName.getText().toString());
        user.setBio(bio.getText().toString());
        user.setLocation(city.getText().toString());
        user.setContact(phoneNumber.getText().toString());
        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child("users").child(uid).child("displayName").setValue(user.getDisplayName());
        mDatabase.child("users").child(uid).child("profileImgPath").setValue(user.getProfileImgPath());
        mDatabase.child("users").child(uid).child("location").setValue(user.getLocation());
        mDatabase.child("users").child(uid).child("bio").setValue(user.getBio());
        mDatabase.child("users").child(uid).child("contact").setValue(user.getContact());

    }

    private void openPhotoChoice() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                runUpload(inputStream);
            }catch (Exception e){

            }
        }
    }

    public void runUpload(InputStream stream){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String imageName = user.getDisplayName().replace(' ', '-') + '-' + timeStamp;

        final StorageReference profilePhotosRef = storageRef.child("profile_images/" + imageName);

        UploadTask uploadTask = profilePhotosRef.putStream(stream);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                profilePhotosRef.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                CircleImageView profilePhoto =  contentLayout.findViewById(R.id.userProfileImage);
                                user.setProfileImgPath(uri.toString());
                                Picasso.get().load(user.getProfileImgPath()).placeholder(R.drawable.avatar).error(R.drawable.ic_launcher_foreground).into(profilePhoto);
                            }});


        }
        });
    }

}
