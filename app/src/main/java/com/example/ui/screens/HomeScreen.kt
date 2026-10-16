package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import com.example.util.ShareHelper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.R
import com.example.data.model.AiNewsItem
import com.example.data.model.AiTool
import com.example.data.repository.AiNewsRepository
import com.example.data.repository.AiToolRepository
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.MainViewModel

data class CategoryFilterItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var selectedNewsItem by remember { mutableStateOf<AiNewsItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadNews(context = context)
    }

    val categoryFilterItems = listOf(
        CategoryFilterItem("All", "All", Icons.Default.GridView),
        CategoryFilterItem("Chatbots", "Chatbots", Icons.Default.ChatBubbleOutline),
        CategoryFilterItem("Content", "Content", Icons.Default.Description),
        CategoryFilterItem("Images", "Images", Icons.Default.Image),
        CategoryFilterItem("Video", "Video", Icons.Default.PlayCircleOutline),
        CategoryFilterItem("Coding", "Coding", Icons.Default.Code)
    )

    // Real-time filtered tools based on search query and category
    val filteredTools = remember(searchQuery, selectedCategoryFilter) {
        val baseTools = if (selectedCategoryFilter == "All") {
            AiToolRepository.tools
        } else {
            AiToolRepository.getToolsByCategory(selectedCategoryFilter)
        }

        if (searchQuery.isBlank()) {
            baseTools
        } else {
            val q = searchQuery.trim().lowercase()
            baseTools.filter { tool ->
                tool.name.lowercase().contains(q) ||
                    tool.tagline.lowercase().contains(q) ||
                    tool.description.lowercase().contains(q) ||
                    tool.category.lowercase().contains(q) ||
                    tool.pricing.lowercase().contains(q) ||
                    tool.tags.any { it.lowercase().contains(q) }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.initSavedArticles(context)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("home_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. App Header: Mascot + "PickAI" + Subtitle + Settings Icon
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_pickai_mascot),
                            contentDescription = "PickAI Mascot",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pick",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "AI",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4F46E5)
                            )
                        }
                        Text(
                            text = "Explore & Master AI Tools",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                IconButton(
                    onClick = { navController.navigate(Screen.Settings.route) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Search Bar: Clean pill with search icon and tune/filter icon
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF4F46E5),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.updateSearchQuery(it)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_field"),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A)),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search tools, categories, pricing...",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                viewModel.updateSearchQuery("")
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 3. Daily AI Pulse Section (Visible when not actively searching)
        if (searchQuery.isBlank() && selectedCategoryFilter == "All") {
            // Header: Calendar icon in box + "Daily AI Pulse" + "Updated Today" badge (guaranteed 1-line)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF6366F1),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Daily AI Pulse",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                maxLines = 1
                            )
                            Text(
                                text = "Today's top breakthroughs",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (viewModel.isNewsOffline) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = if (viewModel.isNewsOffline) Color(0xFFD97706) else Color(0xFF059669),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (viewModel.isNewsOffline) "Offline Cache" else "Live News",
                                    fontSize = 11.sp,
                                    color = if (viewModel.isNewsOffline) Color(0xFFD97706) else Color(0xFF059669),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        IconButton(
                            onClick = { viewModel.loadNews(context = context, forceRefresh = true) },
                            modifier = Modifier.size(32.dp).testTag("refresh_news_button")
                        ) {
                            if (viewModel.isNewsLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(15.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4F46E5)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh news",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Featured Hero Card: Purple-to-Cyan gradient with 3D mascot, Top Story badge, and white CTA button
            val topStory = viewModel.dailyNews.firstOrNull()
            if (topStory != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedNewsItem = topStory }
                            .testTag("news_hero_card"),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF5B3EE8),
                                            Color(0xFF4F46E5),
                                            Color(0xFF38BDF8)
                                        )
                                    )
                                )
                                .padding(18.dp)
                        ) {
                            Column {
                                // Top row: "🔥 Top Story" badge & Source • Time & Bookmark Button
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFF312E81).copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "🔥 Top Story",
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "${topStory.source} • ${topStory.timeAgo}",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Medium
                                        )

                                        IconButton(
                                            onClick = { ShareHelper.shareArticle(context, topStory) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x33FFFFFF))
                                                .testTag("share_top_story_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share article",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        val isTopStorySaved = viewModel.isArticleSaved(topStory)
                                        IconButton(
                                            onClick = { viewModel.toggleSaveArticle(context, topStory) },
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(if (isTopStorySaved) Color(0x44FBBF24) else Color(0x33FFFFFF))
                                                .testTag("bookmark_top_story_button")
                                        ) {
                                            Icon(
                                                imageVector = if (isTopStorySaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = if (isTopStorySaved) "Unsave article" else "Save article",
                                                tint = if (isTopStorySaved) Color(0xFFFBBF24) else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Center Row: Headline & Summary on left, Mascot on right
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = topStory.title,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            lineHeight = 22.sp
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = topStory.summary,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            lineHeight = 17.sp,
                                            maxLines = 3
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Image(
                                        painter = painterResource(id = R.drawable.ic_pickai_mascot),
                                        contentDescription = "Mascot",
                                        modifier = Modifier.size(86.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Bottom Row: White CTA pill & Reading time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color.White
                                    ) {
                                        Text(
                                            text = "Read Full Analysis →",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4F46E5),
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = topStory.readTime,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Secondary Stories: 2 Side-by-side cards (Model Release & On-Device AI) matching screenshot exactly
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val secondaryNews = viewModel.dailyNews.drop(1).take(2)
                    secondaryNews.forEachIndexed { index, news ->
                        val isFirstCard = index == 0
                        val cardBg = if (isFirstCard) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                        val cardBorder = if (isFirstCard) Color(0xFFCCFBF1) else Color(0xFFFEF3C7)
                        val badgeBg = if (isFirstCard) Color(0xFFCCFBF1) else Color(0xFFFEF3C7)
                        val badgeText = if (isFirstCard) Color(0xFF0D9488) else Color(0xFFD97706)
                        val sourceColor = if (isFirstCard) Color(0xFF2563EB) else Color(0xFFD97706)
                        val sourceIconBg = if (isFirstCard) Color(0xFFDBEAFE) else Color(0xFFFFEDD5)
                        val sourceIcon = if (isFirstCard) Icons.Default.PlayArrow else Icons.Default.Memory

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedNewsItem = news }
                                .testTag("news_card_${news.id}"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = BorderStroke(1.dp, cardBorder),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Top row: Badge + Time
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeBg
                                    ) {
                                        Text(
                                            text = news.categoryTag,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeText,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = news.timeAgo,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )

                                        IconButton(
                                            onClick = { ShareHelper.shareArticle(context, news) },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("share_secondary_${news.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share article",
                                                tint = Color(0xFF64748B),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        val isSecondarySaved = viewModel.isArticleSaved(news)
                                        IconButton(
                                            onClick = { viewModel.toggleSaveArticle(context, news) },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("bookmark_secondary_${news.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (isSecondarySaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = if (isSecondarySaved) "Unsave article" else "Save article",
                                                tint = if (isSecondarySaved) MaterialTheme.colorScheme.primary else Color(0xFF94A3B8),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Headline
                                Text(
                                    text = news.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    lineHeight = 18.sp,
                                    maxLines = 3,
                                    modifier = Modifier.heightIn(min = 54.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Bottom row: Source Icon + Name + Arrow
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(sourceIconBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = sourceIcon,
                                                contentDescription = null,
                                                tint = sourceColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }

                                        Text(
                                            text = news.source,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = sourceColor,
                                            maxLines = 1
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = sourceColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Category Filter Chips Row (Pill with icon matching reference screenshot)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categoryFilterItems) { catItem ->
                    val isSelected = selectedCategoryFilter == catItem.id

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF3B82F6) else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .clickable { selectedCategoryFilter = catItem.id }
                            .testTag("filter_chip_${catItem.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = catItem.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = catItem.label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF334155)
                            )
                        }
                    }
                }
            }
        }

        // 5. Curated AI Tools Section Header: Sparkle Icon + Title + "See All →"
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEEF2FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Curated AI Tools",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Showing ${filteredTools.size} tools",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                TextButton(
                    onClick = {
                        searchQuery = ""
                        selectedCategoryFilter = "All"
                    }
                ) {
                    Text(
                        text = "See All →",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3B82F6)
                    )
                }
            }
        }

        // 6. Tool Cards List (Matching reference image: logo squircle, title, category pill, pricing pill, tagline, circular heart)
        if (filteredTools.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No AI tools found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Try searching for other terms like \"code\", \"image\", or \"chat\".",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                searchQuery = ""
                                selectedCategoryFilter = "All"
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Clear Filters")
                        }
                    }
                }
            }
        } else {
            items(filteredTools, key = { it.id }) { tool ->
                val isFav = viewModel.isFavorite(tool.id)

                val toolGradient = when (tool.category.lowercase()) {
                    "chatbots" -> Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF38BDF8)))
                    "content" -> Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                    "images" -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFFF43F5E)))
                    "video" -> Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFEAB308)))
                    "coding" -> Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF10B981)))
                    "audio" -> Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF6366F1)))
                    else -> Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF06B6D4)))
                }
                val dotColor = when (tool.category.lowercase()) {
                    "chatbots" -> Color(0xFF38BDF8)
                    "content" -> Color(0xFFF472B6)
                    "images" -> Color(0xFFFB7185)
                    "video" -> Color(0xFFFBBF24)
                    "coding" -> Color(0xFF34D399)
                    "audio" -> Color(0xFFC084FC)
                    else -> Color(0xFF818CF8)
                }
                val categoryBg = when (tool.category.lowercase()) {
                    "chatbots" -> Color(0xFFEEF2FF)
                    "content" -> Color(0xFFFDF2F8)
                    "images" -> Color(0xFFFFF1F2)
                    "video" -> Color(0xFFFFF7ED)
                    "coding" -> Color(0xFFECFDF5)
                    "audio" -> Color(0xFFFAF5FF)
                    else -> Color(0xFFF1F5F9)
                }
                val categoryTint = when (tool.category.lowercase()) {
                    "chatbots" -> Color(0xFF4F46E5)
                    "content" -> Color(0xFFBE185D)
                    "images" -> Color(0xFFE11D48)
                    "video" -> Color(0xFFC2410C)
                    "coding" -> Color(0xFF059669)
                    "audio" -> Color(0xFF7C3AED)
                    else -> Color(0xFF475569)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate(Screen.ToolDetail.createRoute(tool.id)) }
                        .testTag("tool_card_${tool.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Colorful Avatar Box with vibrant fluid dot (No message/chat icon)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(toolGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tool.name.take(1).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            // Colorful fluid dot at top corner
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .padding(1.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Middle Content: Compact 2-line layout that never spills downwards
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = tool.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = tool.pricing,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = categoryBg
                                ) {
                                    Text(
                                        text = tool.category,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = categoryTint,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Text(
                                    text = tool.tagline,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Circular Saved Bookmark Button
                        IconButton(
                            onClick = { viewModel.toggleFavorite(tool.id) },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isFav) Color(0xFFEEF2FF) else Color(0xFFF8FAFC))
                                .testTag("fav_button_${tool.id}")
                        ) {
                            Icon(
                                imageVector = if (isFav) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isFav) "Saved" else "Save tool",
                                tint = if (isFav) Color(0xFF4F46E5) else Color(0xFF94A3B8),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Daily News Detailed Breakdown Modal Dialog
    if (selectedNewsItem != null) {
        val news = selectedNewsItem!!
        val isBookmarked = viewModel.isArticleSaved(news)

        Dialog(onDismissRequest = { selectedNewsItem = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = news.categoryTag,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { ShareHelper.shareArticle(context, news) },
                                modifier = Modifier.testTag("dialog_share_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share news",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleSaveArticle(context, news) },
                                modifier = Modifier.testTag("dialog_bookmark_button")
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isBookmarked) "Unsave news" else "Save news",
                                    tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { selectedNewsItem = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = news.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "${news.source} • ${news.timeAgo}${if (news.formattedDateTime.isNotBlank()) " • ${news.formattedDateTime}" else ""} • ${news.readTime}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = news.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 22.sp
                        )

                        HorizontalDivider()

                        Text(
                            text = "💡 Key Takeaways",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            news.keyTakeaways.forEach { takeaway ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Text(
                                        text = takeaway,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        HorizontalDivider()

                        Text(
                            text = "In-Depth Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = news.fullAnalysis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        // Related Tools In App Shortcut
                        if (news.relatedToolIds.isNotEmpty()) {
                            HorizontalDivider()

                            Text(
                                text = "Explore Related AI Tools in PickAI:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(news.relatedToolIds) { toolId ->
                                    val tool = viewModel.getToolById(toolId)
                                    if (tool != null) {
                                        SuggestionChip(
                                            onClick = {
                                                selectedNewsItem = null
                                                navController.navigate(Screen.ToolDetail.createRoute(tool.id))
                                            },
                                            label = { Text(tool.name) },
                                            icon = {
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { ShareHelper.shareArticle(context, news) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("modal_share_article_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }

                        if (news.url.isNotBlank()) {
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(news.url))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                },
                                modifier = Modifier
                                    .weight(1.6f)
                                    .testTag("open_source_article_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Read Source", maxLines = 1)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { selectedNewsItem = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done Reading")
                    }
                }
            }
        }
    }
}
