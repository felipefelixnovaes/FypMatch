package com.ideiassertiva.FypMatch.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ideiassertiva.FypMatch.R
import com.ideiassertiva.FypMatch.ui.theme.FypColors

/**
 * Marca FypMatch — coração estilizado (rosa→roxo).
 *
 * @param white quando true usa a versão branca (para fundos com gradiente/escuros);
 *              caso contrário usa a versão com o gradiente da marca (para fundos claros).
 */
@Composable
fun FypHeartMark(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    white: Boolean = false
) {
    Image(
        painter = painterResource(
            id = if (white) R.drawable.ic_brand_heart_white else R.drawable.ic_brand_heart
        ),
        contentDescription = "FypMatch",
        modifier = modifier.size(size)
    )
}

/**
 * Wordmark FypMatch.
 *
 * @param twoTone quando true, "Fyp" em rosa + "Match" em roxo (para fundos claros).
 *                Quando false, a palavra inteira em [solidColor] (ex.: branco sobre gradiente).
 */
@Composable
fun FypMatchWordmark(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 28.sp,
    twoTone: Boolean = true,
    solidColor: Color = Color.White,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val text = buildAnnotatedString {
        if (twoTone) {
            withStyle(SpanStyle(color = FypColors.Primary)) { append("Fyp") }
            withStyle(SpanStyle(color = FypColors.Secondary)) { append("Match") }
        } else {
            withStyle(SpanStyle(color = solidColor)) { append("FypMatch") }
        }
    }
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        letterSpacing = (-0.5).sp,
        modifier = modifier
    )
}
