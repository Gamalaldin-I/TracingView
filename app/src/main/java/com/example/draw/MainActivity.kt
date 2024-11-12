package com.example.draw
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.draw.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.tracingView.setNewPathArray(Paths.getA())

        binding.check.setOnClickListener{

            if(binding.tracingView.checkAllPathsCompleted())
            {
                Toast.makeText(this, "Bravo", Toast.LENGTH_LONG).show()
            }
            else{
                Toast.makeText(this, "continue", Toast.LENGTH_LONG).show()
            }

        }
        binding.reset.setOnClickListener{
            binding.tracingView.reset()
        }

}
}




