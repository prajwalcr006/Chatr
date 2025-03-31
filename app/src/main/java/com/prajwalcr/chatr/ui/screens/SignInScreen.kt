package com.prajwalcr.chatr.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prajwalcr.chatr.R

@Composable
fun SignInScreen(onSignInClick: () -> Unit) {
    val customBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xC0C0C0),
            Color(0xC0C0C0)
        )
    )
    //takes full screen
    Box {
        Image(
            painter = painterResource(R.drawable.login_blur),
            contentDescription = "Sign in Screen",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(80.dp))
            Image(
                painter = painterResource(R.drawable.oig4__rndcloiljdx4hxpn),
                contentDescription = null,
            )

            Text(
                text = "Chatr",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center
            )

            Text(
                text = "A simple chat app for fun !!",
                color = Color(color = 0xFF101010),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(80.dp))

            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .background(
                        brush = customBrush
                    )
                    .fillMaxWidth(0.7f)
                    .height(50.dp),
                shape = CircleShape
            ) {
                Text(
                    text = "continue with google",
                    modifier = Modifier.padding(end = 20.dp),
                    color =  Color.LightGray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )

                Image(
                    painter = painterResource(R.drawable.goog_0ed88f7c),
                    contentDescription = "Google image",
                    modifier = Modifier.scale(1f)
                )
            }

        }
    }
}