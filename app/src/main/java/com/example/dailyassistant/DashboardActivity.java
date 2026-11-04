package com.example.dailyassistant;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;


public class DashboardActivity extends AppCompatActivity {


    private View btnHome, btnTask, btnHealth, btnDiary;

    private ImageView imgHome, imgTask, imgHealth, imgDiary;

    private TextView txtHome, txtTask, txtHealth, txtDiary;


    private ImageView imgMenu;
    private ImageView imgNotification;



    private static final int NOTIFICATION_PERMISSION_CODE = 100;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);





        initViews();

        applyStatusBarPadding();

        setupHeaderActions();

        setupBackNavigation();



        selectTab(R.id.btnHome);



        btnHome.setOnClickListener(
                v -> selectTab(R.id.btnHome)
        );


        btnTask.setOnClickListener(
                v -> selectTab(R.id.btnTask)
        );


        btnHealth.setOnClickListener(
                v -> selectTab(R.id.btnHealth)
        );


        btnDiary.setOnClickListener(
                v -> selectTab(R.id.btnDiary)
        );


    }





    private void initViews(){


        btnHome =
                findViewById(R.id.btnHome);

        btnTask =
                findViewById(R.id.btnTask);

        btnHealth =
                findViewById(R.id.btnHealth);

        btnDiary =
                findViewById(R.id.btnDiary);



        imgHome =
                findViewById(R.id.imgHome);

        imgTask =
                findViewById(R.id.imgTask);

        imgHealth =
                findViewById(R.id.imgHealth);

        imgDiary =
                findViewById(R.id.imgDiary);



        txtHome =
                findViewById(R.id.txtHome);

        txtTask =
                findViewById(R.id.txtTask);

        txtHealth =
                findViewById(R.id.txtHealth);

        txtDiary =
                findViewById(R.id.txtDiary);



        imgMenu =
                findViewById(R.id.img_menu);


        imgNotification =
                findViewById(R.id.img_notification);


    }





    private void applyStatusBarPadding(){


        View headerLayout =
                findViewById(R.id.header_layout);


        if(headerLayout == null)
            return;



        int left =
                headerLayout.getPaddingLeft();

        int right =
                headerLayout.getPaddingRight();

        int bottom =
                headerLayout.getPaddingBottom();




        ViewCompat.setOnApplyWindowInsetsListener(
                headerLayout,
                (view,insets)->{


                    Insets statusBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.statusBars()
                            );


                    view.setPadding(
                            left,
                            statusBars.top,
                            right,
                            bottom
                    );


                    return insets;

                }
        );


    }





    private void setupHeaderActions(){


        if(imgMenu != null){


            imgMenu.setOnClickListener(
                    v -> {

                    }
            );

        }




        if(imgNotification != null){


            imgNotification.setOnClickListener(
                    v -> {


                        Intent intent =
                                new Intent(
                                        DashboardActivity.this,
                                        NotificationActivity.class
                                );


                        startActivity(intent);


                    }
            );


        }


    }







    private void setupBackNavigation(){


        getOnBackPressedDispatcher()
                .addCallback(
                        this,
                        new OnBackPressedCallback(true) {


                            @Override
                            public void handleOnBackPressed(){

                                setEnabled(false);

                                getOnBackPressedDispatcher()
                                        .onBackPressed();

                            }


                        }
                );


    }







    private void selectTab(int selectedTabId){


        resetTabs();


        Fragment fragment = null;




        if(selectedTabId == R.id.btnHome){


            imgHome.setImageResource(
                    R.drawable.ic_nav_home_act
            );


            txtHome.setVisibility(
                    View.VISIBLE
            );


            fragment =
                    new HomeFragment();


        }

        else if(selectedTabId == R.id.btnTask){


            imgTask.setImageResource(
                    R.drawable.ic_nav_tasks_act
            );


            txtTask.setVisibility(
                    View.VISIBLE
            );


            fragment =
                    new TaskManagerFragment();


        }


        else if(selectedTabId == R.id.btnHealth){


            imgHealth.setImageResource(
                    R.drawable.ic_nav_health_act
            );


            txtHealth.setVisibility(
                    View.VISIBLE
            );


            fragment =
                    new HealthCareFragment();


        }


        else if(selectedTabId == R.id.btnDiary){


            imgDiary.setImageResource(
                    R.drawable.ic_nav_diary_act
            );


            txtDiary.setVisibility(
                    View.VISIBLE
            );


            fragment =
                    new DiaryFragment();


        }





        if(fragment != null){


            getSupportFragmentManager()

                    .beginTransaction()

                    .replace(
                            R.id.fragment_container,
                            fragment
                    )

                    .commit();


        }


    }







    private void resetTabs(){


        imgHome.setImageResource(
                R.drawable.ic_nav_home
        );


        imgTask.setImageResource(
                R.drawable.ic_nav_tasks
        );


        imgHealth.setImageResource(
                R.drawable.ic_nav_health
        );


        imgDiary.setImageResource(
                R.drawable.ic_nav_diary
        );



        txtHome.setVisibility(
                View.GONE
        );


        txtTask.setVisibility(
                View.GONE
        );


        txtHealth.setVisibility(
                View.GONE
        );


        txtDiary.setVisibility(
                View.GONE
        );


    }







    public void openHomeTab(){

        selectTab(R.id.btnHome);

    }



    public void openTasksTab(){

        selectTab(R.id.btnTask);

    }



    public void openHealthTab(){

        selectTab(R.id.btnHealth);

    }



    public void openDiaryTab(){

        selectTab(R.id.btnDiary);

    }









    private void requestNotificationPermission(){


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){


            if(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            )
                    != PackageManager.PERMISSION_GRANTED){



                ActivityCompat.requestPermissions(

                        this,

                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },

                        NOTIFICATION_PERMISSION_CODE

                );


            }


        }


    }



}