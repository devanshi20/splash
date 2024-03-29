package com.example.atul_.eatit;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.atul_.eatit.Common.Common;
import com.example.atul_.eatit.Database.Database;
import com.example.atul_.eatit.model.Food;
import com.example.atul_.eatit.model.Order;
import com.example.atul_.eatit.model.Rating;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView food_name,food_price,food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    FloatingActionButton btnRating;
    ElegantNumberButton numberButton;
    RatingBar ratingBar;
    SQLiteDatabase db;

    Database d=new Database(FoodDetail.this);




    String foodId="";

    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;
    Food currentFood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);


        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Food");
        ratingTbl = database.getReference("Rating");
        numberButton = (ElegantNumberButton)findViewById(R.id.number_button);

        btnCart = (FloatingActionButton)findViewById(R.id.btnCart);
        btnRating = (FloatingActionButton)findViewById(R.id.btn_rating);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();
            }


        });
        btnCart.setOnClickListener(new View.OnClickListener() {

             @Override

                                            public void onClick(View view) {



                 db=openOrCreateDatabase("EatIt.db", Context.MODE_PRIVATE,null);
                 new Database(getBaseContext()).addToCart(new Order(
                         foodId,
                         currentFood.getName(),
                         numberButton.getNumber(),
                         currentFood.getPrice(),
                         currentFood.getDiscount()

                         ));

                         Toast.makeText(FoodDetail.this, "Added to cart ", Toast.LENGTH_SHORT).show();


                 d.getAData();




             }

        });






        food_description = (TextView)findViewById(R.id.food_description);
        food_price = (TextView)findViewById(R.id.food_price);
        food_name = (TextView)findViewById(R.id.food_name);
        food_image = (ImageView)findViewById(R.id.img_food);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
     // collapsingToolbarLayout = setExpandedTitleAppearance(R.style.ExpandedAppbar);
      //  collapsingToolbarLayout = setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty()) {
       if(Common.isConnectedToInternet(getBaseContext()))
       {
           getDetailFood(foodId);
           getRatingFood(foodId);
       }
        }
     }

    private void getRatingFood(String foodId) {

      com.google.firebase.database.Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count=0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                    {
                        Rating item = postSnapshot.getValue(Rating.class);
                        sum+=Integer.parseInt(item.getRateValue());
                        count++;

                    }
                    if(count!=0)
                    {
                        float average = sum/count;
                        ratingBar.setRating(average);
                    }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void showRatingDialog() {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","NotGood","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please rate us and give the feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Please write your comment here...")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void getDetailFood(String foodId){
           foods.child(foodId).addValueEventListener(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   currentFood=dataSnapshot.getValue(Food.class);


                   Picasso.with(getBaseContext()).load(currentFood.getImage())
                   .into(food_image);
                   collapsingToolbarLayout.setTitle(currentFood.getName());
                   food_price.setText(currentFood.getPrice());
                   food_name.setText(currentFood.getName());
                   food_description.setText(currentFood.getDescription());

               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

               }
           });
    }


    @Override
    public void onPositiveButtonClicked(int value, String comments) {
        final Rating rating = new Rating(Common.currentUser.getPhone(),
                foodId,
                String.valueOf(value),
                comments);
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
            if(dataSnapshot.child(Common.currentUser.getPhone()).exists())
            {
                ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);

            }
            else {
                ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
            }
            Toast.makeText(FoodDetail.this,"Thank you!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }
}
