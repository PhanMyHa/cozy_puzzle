package com.example.cozypuzzle.data.datasource

import com.example.cozypuzzle.R
import com.example.cozypuzzle.domain.model.mycats.CatModel
import com.example.cozypuzzle.domain.model.mycats.CatRarity
import com.example.cozypuzzle.domain.model.mycats.CatTheme
import com.example.cozypuzzle.domain.model.mycats.CatUnlockCondition
import com.example.cozypuzzle.domain.model.mycats.UnlockGameMode
import com.example.cozypuzzle.data.remote.datasource.GameRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundledCatCatalogDataSource @Inject constructor() {

    @Inject
    lateinit var gameRemoteDataSource: GameRemoteDataSource

    private val cats = listOf(
        // Theme: Sleepy Cats
        CatModel(
            id = "cat_milo",
            name = "Milo",
            imageRes = R.drawable.img_cat_milo,
            rarity = CatRarity.COMMON,
            theme = CatTheme.SLEEPY,
            description = "Milo loves to sleep in sunny spots and dreams of a big adventure!",
            foundIn = "Available from start",
            unlockCondition = CatUnlockCondition.DefaultUnlocked,
            unlockHint = "Available from start"
        ),
        CatModel(
            id = "cat_luna",
            name = "Luna",
            imageRes = R.drawable.img_cat_luna,
            rarity = CatRarity.COMMON,
            theme = CatTheme.SLEEPY,
            description = "Luna loves curling up on cozy blankets while listening to rain.",
            foundIn = "Cat Puzzle Lv. 1",
            unlockCondition = CatUnlockCondition.CompleteSpecificLevel(UnlockGameMode.CAT_PUZZLE, 1, "cat_1"),
            unlockHint = "Complete Cat Puzzle Level 1"
        ),
        CatModel(
            id = "cat_snow",
            name = "Snow",
            imageRes = R.drawable.img_cat_snow,
            rarity = CatRarity.RARE,
            theme = CatTheme.SLEEPY,
            description = "Snow is a quiet kitten who loves fluffy cushions and gentle pets.",
            foundIn = "Complete 3 levels",
            unlockCondition = CatUnlockCondition.CompleteTotalLevels(3),
            unlockHint = "Complete 3 levels"
        ),

        // Theme: Chef Cats
        CatModel(
            id = "cat_oliver",
            name = "Oliver",
            imageRes = R.drawable.img_cat_oliver,
            rarity = CatRarity.EPIC,
            theme = CatTheme.CHEF,
            description = "Oliver is a master pastry chef making miniature fish pies!",
            foundIn = "Color the Cat Lv. 1",
            unlockCondition = CatUnlockCondition.CompleteSpecificLevel(UnlockGameMode.COLOR_THE_CAT, 1, "color_1"),
            unlockHint = "Complete Color the Cat Level 1"
        ),
        CatModel(
            id = "cat_coco",
            name = "Coco",
            imageRes = R.drawable.img_cat_coco,
            rarity = CatRarity.RARE,
            theme = CatTheme.CHEF,
            description = "Coco loves taste-testing warm sweet milk and honey buns.",
            foundIn = "Collect 6 stars",
            unlockCondition = CatUnlockCondition.EarnTotalStars(6),
            unlockHint = "Collect 6 stars"
        ),
        CatModel(
            id = "cat_waffles",
            name = "Waffles",
            imageRes = R.drawable.img_cat_waffles,
            rarity = CatRarity.COMMON,
            theme = CatTheme.CHEF,
            description = "Waffles wears a tiny apron and flips fish-shaped pancakes.",
            foundIn = "Slide the Cat Lv. 1",
            unlockCondition = CatUnlockCondition.CompleteSpecificLevel(UnlockGameMode.SLIDE_THE_CAT, 1, "slide_easy_1"),
            unlockHint = "Complete Slide the Cat Level 1"
        ),

        // Theme: Space Cats
        CatModel(
            id = "cat_leo",
            name = "Leo",
            imageRes = R.drawable.img_cat_leo,
            rarity = CatRarity.EPIC,
            theme = CatTheme.SPACE,
            description = "Leo explores the galaxy in his cozy little bubble astronaut helmet!",
            foundIn = "Find the Pair Lv. 1",
            unlockCondition = CatUnlockCondition.CompleteSpecificLevel(UnlockGameMode.FIND_THE_PAIR, 1, "pair_easy_1"),
            unlockHint = "Complete Find the Pair Level 1"
        ),
        CatModel(
            id = "cat_nova",
            name = "Nova",
            imageRes = R.drawable.img_cat_nova,
            rarity = CatRarity.RARE,
            theme = CatTheme.SPACE,
            description = "Nova catches shooting star dust in jars and dreams of the moon.",
            foundIn = "Complete 6 levels",
            unlockCondition = CatUnlockCondition.CompleteTotalLevels(6),
            unlockHint = "Complete 6 levels"
        ),
        CatModel(
            id = "cat_comet",
            name = "Comet",
            imageRes = R.drawable.img_cat_comet,
            rarity = CatRarity.LEGENDARY,
            theme = CatTheme.SPACE,
            description = "Comet zooms across the night sky leaving trails of sparkles.",
            foundIn = "Collect 18 stars",
            unlockCondition = CatUnlockCondition.EarnTotalStars(18),
            unlockHint = "Collect 18 stars"
        ),

        // Theme: Garden Cats
        CatModel(
            id = "cat_bella",
            name = "Bella",
            imageRes = R.drawable.img_cat_bella,
            rarity = CatRarity.RARE,
            theme = CatTheme.GARDEN,
            description = "Bella loves napping among chamomile blossoms and butterflies.",
            foundIn = "Cat Puzzle Lv. 2",
            unlockCondition = CatUnlockCondition.CompleteSpecificLevel(UnlockGameMode.CAT_PUZZLE, 2, "cat_2"),
            unlockHint = "Complete Cat Puzzle Level 2"
        ),
        CatModel(
            id = "cat_clover",
            name = "Clover",
            imageRes = R.drawable.img_cat_clover,
            rarity = CatRarity.COMMON,
            theme = CatTheme.GARDEN,
            description = "Clover is a lucky calico cat who always finds four-leaf clovers.",
            foundIn = "Available from start",
            unlockCondition = CatUnlockCondition.DefaultUnlocked,
            unlockHint = "Available from start"
        ),
        CatModel(
            id = "cat_pudding",
            name = "Pudding",
            imageRes = R.drawable.img_cat_pudding,
            rarity = CatRarity.LEGENDARY,
            theme = CatTheme.GARDEN,
            description = "Pudding is a curious cat who loves exploring new magical places!",
            foundIn = "Complete 10 levels",
            unlockCondition = CatUnlockCondition.CompleteTotalLevels(10),
            unlockHint = "Complete 10 levels"
        )
    )

    private val allActiveCats: List<CatModel>
        get() {
            val remote = if (::gameRemoteDataSource.isInitialized) {
                gameRemoteDataSource.getCats()
            } else {
                null
            }
            return if (!remote.isNullOrEmpty()) remote else cats
        }

    fun getAllCats(): List<CatModel> = allActiveCats

    fun getCatById(id: String): CatModel? = allActiveCats.firstOrNull { it.id == id }

    fun getDefaultUnlockedIds(): Set<String> =
        allActiveCats.filter { it.unlockCondition is CatUnlockCondition.DefaultUnlocked }.map { it.id }.toSet()

    companion object {
        val DEFAULT_FALLBACK_IMAGE_RES = R.drawable.img_cat_milo
    }
}
