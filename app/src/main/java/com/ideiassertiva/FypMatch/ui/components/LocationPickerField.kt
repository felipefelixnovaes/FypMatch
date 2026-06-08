package com.ideiassertiva.FypMatch.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ideiassertiva.FypMatch.data.BrazilLocationCatalog
import com.ideiassertiva.FypMatch.data.DeviceLocationResolver
import com.ideiassertiva.FypMatch.data.displayName
import com.ideiassertiva.FypMatch.model.Location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerField(
    location: Location,
    onLocationChange: (Location) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowCurrentLocation: Boolean = true,
    supportingText: String? = null
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf(location.displayName()) }
    var expanded by remember { mutableStateOf(false) }
    var isResolvingLocation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun useCurrentLocation() {
        isResolvingLocation = true
        val result = DeviceLocationResolver.resolveCurrentCity(context)
        isResolvingLocation = false
        result.fold(
            onSuccess = { current ->
                errorMessage = null
                query = current.displayName()
                onLocationChange(current)
            },
            onFailure = { error ->
                errorMessage = error.message ?: "Não foi possível usar sua localização."
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            useCurrentLocation()
        } else {
            errorMessage = "Permissão de localização negada."
        }
    }

    LaunchedEffect(location.city, location.state) {
        val labelValue = location.displayName()
        if (labelValue.isNotBlank() && labelValue != query) {
            query = labelValue
        }
    }

    val suggestions = remember(query) {
        BrazilLocationCatalog.suggestions(query)
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded && suggestions.isNotEmpty(),
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { value ->
                    query = value
                    expanded = true
                    errorMessage = null
                    onLocationChange(Location(city = value.trim()))
                },
                label = { Text(label) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                trailingIcon = {
                    Row {
                        if (allowCurrentLocation) {
                            IconButton(
                                onClick = {
                                    if (DeviceLocationResolver.hasLocationPermission(context)) {
                                        useCurrentLocation()
                                    } else {
                                        permissionLauncher.launch(DeviceLocationResolver.requiredPermissions)
                                    }
                                },
                                enabled = !isResolvingLocation
                            ) {
                                if (isResolvingLocation) {
                                    CircularProgressIndicator(modifier = Modifier.padding(10.dp))
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MyLocation,
                                        contentDescription = "Usar localização atual"
                                    )
                                }
                            }
                        }
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded && suggestions.isNotEmpty(),
                onDismissRequest = { expanded = false }
            ) {
                suggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion.displayName()) },
                        onClick = {
                            expanded = false
                            errorMessage = null
                            query = suggestion.displayName()
                            onLocationChange(suggestion)
                        }
                    )
                }
            }
        }

        if (supportingText != null) {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (location.city.isNotBlank() && location.state.isNotBlank()) {
            AssistChip(
                onClick = { },
                label = { Text(location.displayName()) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null
                    )
                }
            )
        }
    }
}
