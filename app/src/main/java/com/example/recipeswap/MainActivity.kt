package com.example.recipeswap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.recipeswap.ui.theme.RecipeSwapTheme
import com.example.recipeswap.ui.theme.view.LoginScreen
import com.example.recipeswap.ui.theme.view.MainScreenWithMenu
import com.example.recipeswap.ui.theme.viewmodel.RecipeViewModel

class MainActivity : ComponentActivity() {

    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipeSwapTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val isLoggedIn by recipeViewModel.isLoggedIn.collectAsState()
                    if (isLoggedIn) {
                        MainScreenWithMenu(viewModel = recipeViewModel)
                    } else {
                        LoginScreen(viewModel = recipeViewModel)
                    }
                }
            }
        }
    }
}
