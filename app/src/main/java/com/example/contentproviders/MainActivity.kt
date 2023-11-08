package com.example.contentproviders

import android.Manifest
import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import coil.compose.AsyncImage
import com.example.contentproviders.ui.theme.ContentProvidersTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {

   private val viewModel by viewModels<ImageViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request the READ_MEDIA_IMAGES permission to access images
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            0
        )

        // Define the columns to retrieve from the MediaStore for each image
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
        )

        // Calculate a timestamp for "yesterday" and create a selection string
        val millisYesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR,-1)
        }.timeInMillis
        val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?"
        val selectionArgs = arrayOf(millisYesterday.toString())

        // Define the sorting order for retrieved images
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        // Query the MediaStore to retrieve images taken within the last day
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
            // A cursor is really just used to iterate over data set
        )?.use { cursor ->
            // Get the column indices for ID and name
            val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)

            val images = mutableListOf<Image>()
            while(cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                images.add(Image(id,name,uri))
            }

            // Update the ViewModel with the list of images
            viewModel.updateImages(images)
        }

        // Set up the Compose UI
        setContent {
            ContentProvidersTheme {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Iterate through the list of images
                    items(viewModel.images) { image ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Display each image using AsyncImage
                            AsyncImage(
                                model = image.uri,
                                contentDescription = null
                            )
                            // Display the image's name
                            Text(text = image.name)
                        }
                    }
                }
            }
        }
    }
}

data class Image(
    val id: Long,
    val name: String,
    val uri: Uri
)