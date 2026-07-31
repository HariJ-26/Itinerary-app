package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HighDensityBackground
import com.example.ui.theme.HighDensityBadgeBg
import com.example.ui.theme.HighDensityOnPrimary
import com.example.ui.theme.HighDensityOutline
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensityTextMain
import com.example.ui.theme.HighDensityTextMuted
import com.example.ui.theme.HighDensityTextVariant
import com.example.ui.theme.HighDensityVerifiedGreen
import com.example.ui.theme.TravelPlannerTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TravelPlannerTheme {
        TravelPlannerApp()
      }
    }
  }
}

@Composable
fun TravelPlannerApp() {
  var selectedTab by remember { mutableIntStateOf(0) }
  var selectedDay by remember { mutableIntStateOf(0) }
  var rainAlertRerouted by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = HighDensityBackground,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    bottomBar = {
      HighDensityBottomBar(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it }
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Spacer(modifier = Modifier.height(8.dp))

      // Header Section
      HighDensityHeader(
        onNotificationClick = {
          scope.launch { snackbarHostState.showSnackbar("No new real-time alerts.") }
        },
        onSettingsClick = {
          scope.launch { snackbarHostState.showSnackbar("Settings & AI Grounding configuration opened.") }
        }
      )

      when (selectedTab) {
        0 -> {
          // Plan Screen Content
          HighDensityDaySelector(
            selectedDay = selectedDay,
            onDaySelected = { selectedDay = it }
          )

          // Ground Truth Map Section
          HighDensityMapSection()

          // Adaptive Itinerary Section
          HighDensityItinerarySection(
            rainAlertRerouted = rainAlertRerouted,
            onAcceptReroute = {
              rainAlertRerouted = true
              scope.launch {
                snackbarHostState.showSnackbar("Itinerary dynamically updated to indoor venue! Ground truth verified.")
              }
            }
          )
        }
        1 -> HighDensityBrowseScreen()
        2 -> HighDensityCollabScreen()
        3 -> HighDensityAccountScreen()
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
fun HighDensityHeader(
  onNotificationClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .background(HighDensitySurface, shape = RoundedCornerShape(24.dp))
      .border(1.dp, HighDensityOutline, shape = RoundedCornerShape(24.dp))
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(HighDensityPrimary),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "A",
          color = HighDensityOnPrimary,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }

      Column {
        Text(
          text = "Aether Architect",
          color = HighDensityTextMain,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(HighDensityVerifiedGreen)
          )
          Text(
            text = "LIVE AI ENGINE",
            color = HighDensityPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )
        }
      }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      IconButton(
        onClick = onNotificationClick,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(HighDensityOutline)
          .testTag("notification_button")
      ) {
        Icon(
          imageVector = Icons.Default.Notifications,
          contentDescription = "Notifications",
          tint = HighDensityTextMain,
          modifier = Modifier.size(20.dp)
        )
      }
      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(HighDensityOutline)
          .testTag("settings_button")
      ) {
        Icon(
          imageVector = Icons.Default.Settings,
          contentDescription = "Settings",
          tint = HighDensityTextMain,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun HighDensityDaySelector(
  selectedDay: Int,
  onDaySelected: (Int) -> Unit
) {
  val days = listOf("DAY 01", "DAY 02", "DAY 03", "DAY 04")
  LazyRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    itemsIndexed(days) { index, dayLabel ->
      val isSelected = index == selectedDay
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .background(if (isSelected) HighDensityPrimary else HighDensityOutline)
          .clickable { onDaySelected(index) }
          .padding(horizontal = 16.dp, vertical = 8.dp)
          .testTag("day_${index + 1}_chip")
      ) {
        Text(
          text = dayLabel,
          color = if (isSelected) HighDensityOnPrimary else HighDensityTextMain,
          fontSize = 12.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
      }
    }
  }
}

@Composable
fun HighDensityMapSection() {
  val coffeeLocation = LatLng(40.7233, -73.9985)
  val momaLocation = LatLng(40.7248, -73.9972)
  val parkLocation = LatLng(40.7308, -73.9973)

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(coffeeLocation, 14.5f)
  }

  var mapMode by remember { mutableStateOf(0) } // 0: Native SDK, 1: Ground Truth Vector Grid
  val isDefaultKey = BuildConfig.MAPS_API_KEY.contains("DEFAULT") || BuildConfig.MAPS_API_KEY.isBlank()

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("ground_truth_map"),
    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
    shape = RoundedCornerShape(24.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (mapMode == 0) "GROUND TRUTH MAP (NATIVE SDK)" else "GROUND TRUTH VECTOR GRID",
          color = HighDensityTextVariant,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )

        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (mapMode == 0) HighDensityPrimary else HighDensityOutline)
              .clickable { mapMode = 0 }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Native",
              color = if (mapMode == 0) HighDensityOnPrimary else HighDensityTextMain,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (mapMode == 1) HighDensityPrimary else HighDensityOutline)
              .clickable { mapMode = 1 }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "Vector Grid",
              color = if (mapMode == 1) HighDensityOnPrimary else HighDensityTextMain,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      if (isDefaultKey) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(HighDensityBackground, shape = RoundedCornerShape(8.dp))
            .border(1.dp, HighDensityOutline, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = HighDensityVerifiedGreen,
            modifier = Modifier.size(12.dp)
          )
          Text(
            text = "Google Maps SDK initialized. Add MAPS_API_KEY in AI Studio Secrets for satellite tiles.",
            color = HighDensityTextMuted,
            fontSize = 9.sp
          )
        }
      }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(200.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(HighDensityBackground)
          .border(1.dp, HighDensityOutline, shape = RoundedCornerShape(16.dp))
      ) {
        if (mapMode == 0) {
          GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
              zoomControlsEnabled = true,
              compassEnabled = true
            )
          ) {
            Marker(
              state = rememberMarkerState(position = coffeeLocation),
              title = "Artisanal Coffee & Roastery",
              snippet = "Current Stop — Verified Open"
            )
            Marker(
              state = rememberMarkerState(position = momaLocation),
              title = "MoMA Design Store",
              snippet = "Indoor Gallery Option"
            )
            Marker(
              state = rememberMarkerState(position = parkLocation),
              title = "Washington Square Park",
              snippet = "Outdoor Sculpture Walk"
            )
            Polyline(
              points = listOf(coffeeLocation, momaLocation, parkLocation),
              color = HighDensityPrimary,
              width = 8f
            )
          }
        } else {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSpacing = 24.dp.toPx()
            val pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

            var x = 0f
            while (x < size.width) {
              drawLine(
                color = HighDensityOutline.copy(alpha = 0.3f),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                pathEffect = pathEffect
              )
              x += gridSpacing
            }

            var y = 0f
            while (y < size.height) {
              drawLine(
                color = HighDensityOutline.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                pathEffect = pathEffect
              )
              y += gridSpacing
            }

            val start = Offset(size.width * 0.25f, size.height * 0.7f)
            val cafe = Offset(size.width * 0.45f, size.height * 0.45f)
            val museum = Offset(size.width * 0.75f, size.height * 0.3f)

            drawLine(
              color = HighDensityPrimary.copy(alpha = 0.6f),
              start = start,
              end = cafe,
              strokeWidth = 4f
            )
            drawLine(
              color = HighDensityOutline,
              start = cafe,
              end = museum,
              strokeWidth = 3f,
              pathEffect = pathEffect
            )

            drawCircle(color = HighDensityPrimary, radius = 6f, center = start)
            drawCircle(color = HighDensityPrimary, radius = 10f, center = cafe)
            drawCircle(color = HighDensityPrimary.copy(alpha = 0.5f), radius = 6f, center = museum)
          }
        }

        // Overlay Callout Badge for Current Stop
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(12.dp)
            .background(HighDensitySurface, shape = RoundedCornerShape(8.dp))
            .border(1.dp, HighDensityPrimary, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocalCafe,
              contentDescription = null,
              tint = HighDensityPrimary,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = "Current Stop: Artisanal Coffee",
              color = HighDensityPrimary,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}

@Composable
fun HighDensityItinerarySection(
  rainAlertRerouted: Boolean,
  onAcceptReroute: () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier.padding(horizontal = 4.dp)
    ) {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = HighDensityPrimary,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = "ADAPTIVE ITINERARY",
        color = HighDensityTextVariant,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
      )
    }

    // Active Stop Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("active_itinerary_card"),
      colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
      shape = RoundedCornerShape(24.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityPrimary.copy(alpha = 0.3f))
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.Top
        ) {
          Column {
            Text(
              text = "10:45 AM — NOW",
              color = HighDensityPrimary,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = "Artisanal Coffee & Roastery",
              color = HighDensityTextMain,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = HighDensityTextMuted,
                modifier = Modifier.size(14.dp)
              )
              Text(
                text = "SoHo, Manhattan",
                color = HighDensityTextMuted,
                fontSize = 12.sp
              )
            }
          }

          Box(
            modifier = Modifier
              .background(HighDensityBadgeBg, shape = RoundedCornerShape(8.dp))
              .border(1.dp, HighDensityVerifiedGreen.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "VERIFIED OPEN",
              color = HighDensityVerifiedGreen,
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .background(HighDensityBackground, shape = RoundedCornerShape(12.dp))
              .border(1.dp, HighDensityOutline, shape = RoundedCornerShape(12.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "VIBE MATCH",
                color = HighDensityTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Slow-paced / Industrial",
                color = HighDensityTextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .background(HighDensityBackground, shape = RoundedCornerShape(12.dp))
              .border(1.dp, HighDensityOutline, shape = RoundedCornerShape(12.dp))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "AVG. SPEND",
                color = HighDensityTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "$12 - $18",
                color = HighDensityTextMain,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // AI Weather / Reroute Alert Banner
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(HighDensityBackground, shape = RoundedCornerShape(16.dp))
            .border(1.dp, HighDensityOutline, shape = RoundedCornerShape(16.dp))
            .padding(12.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .border(1.dp, HighDensityPrimary, CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = HighDensityPrimary,
                  modifier = Modifier.size(16.dp)
                )
              }
              Text(
                text = if (rainAlertRerouted)
                  "AI Alert Accepted: Outdoor walk replaced with MoMA Design Store indoor gallery tour."
                else
                  "AI Alert: Light rain starting in 15m. Rerouting next stop to Indoor Gallery.",
                color = HighDensityTextMain,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
              )
            }

            AnimatedVisibility(
              visible = !rainAlertRerouted,
              enter = fadeIn(),
              exit = fadeOut()
            ) {
              Button(
                onClick = onAcceptReroute,
                colors = ButtonDefaults.buttonColors(
                  containerColor = HighDensityPrimary,
                  contentColor = HighDensityOnPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("accept_reroute_button")
              ) {
                Text(
                  text = "Accept Dynamic Reroute",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }
          }
        }
      }
    }

    // Next Stop Preview
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("next_stop_card"),
      colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
      shape = RoundedCornerShape(20.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "NEXT",
            color = HighDensityPrimary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "12:30 PM",
            color = HighDensityTextMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }

        Box(
          modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(HighDensityOutline)
        )

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (rainAlertRerouted) "MoMA Design Store (Indoor)" else "Washington Square Park",
            color = HighDensityTextMain,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = if (rainAlertRerouted) "Indoor Selection • 1.2 mi away" else "Outdoor Sculpture Walk • 0.8 mi away",
            color = HighDensityTextMuted,
            fontSize = 10.sp
          )
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = null,
          tint = HighDensityTextMuted,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

@Composable
fun HighDensityBrowseScreen() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
    shape = RoundedCornerShape(24.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "AI GROUNDED EXPLORER",
        color = HighDensityPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Browse curated vibe recommendations verified through live Google Places API context window.",
        color = HighDensityTextMain,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
fun HighDensityCollabScreen() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
    shape = RoundedCornerShape(24.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "REAL-TIME GROUP COLLABORATION",
        color = HighDensityPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Sync group preferences, live voting on itinerary updates, and shared budget tracking.",
        color = HighDensityTextMain,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
fun HighDensityAccountScreen() {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
    shape = RoundedCornerShape(24.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline)
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "TRAVEL PROFILE & SECRETS",
        color = HighDensityPrimary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
      Text(
        text = "Configured Gemini API Grounding Keys, Google Maps API Integrations & Preferred Travel Pace.",
        color = HighDensityTextMain,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
fun HighDensityBottomBar(
  selectedTab: Int,
  onTabSelected: (Int) -> Unit
) {
  val tabs = listOf(
    NavTab("Plan", Icons.Default.Map),
    NavTab("Browse", Icons.Default.Explore),
    NavTab("Collab", Icons.Default.Group),
    NavTab("Account", Icons.Default.Person)
  )

  Surface(
    color = HighDensitySurface,
    modifier = Modifier
      .fillMaxWidth()
      .windowInsetsPadding(WindowInsets.navigationBars)
      .border(androidx.compose.foundation.BorderStroke(1.dp, HighDensityOutline))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(72.dp)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      tabs.forEachIndexed { index, tab ->
        val isSelected = selectedTab == index
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier = Modifier
            .weight(1f)
            .clickable { onTabSelected(index) }
            .padding(vertical = 4.dp)
            .testTag("tab_${tab.label.lowercase()}")
        ) {
          Box(
            modifier = Modifier
              .width(56.dp)
              .height(32.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSelected) HighDensityPrimary.copy(alpha = 0.15f) else Color.Transparent),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = tab.icon,
              contentDescription = tab.label,
              tint = if (isSelected) HighDensityPrimary else HighDensityTextVariant
            )
          }
          Text(
            text = tab.label,
            color = if (isSelected) HighDensityPrimary else HighDensityTextMuted,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        }
      }
    }
  }
}

data class NavTab(val label: String, val icon: ImageVector)

