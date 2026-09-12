package com.example.tijori.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

@Composable
fun ProfileHeader(
    firstName: String,
    lastName: String?,
    profilePictureURI: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (profilePictureURI != null) {
            AsyncImage(
                model = profilePictureURI,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Profile picture",
                modifier = Modifier.size(96.dp)
            )
        }

        Text(
            text = listOfNotNull(firstName, lastName).joinToString(" "),
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp
            ),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}