package shadowfox.botanicaladdons.common.items

import com.google.common.collect.Multimap
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.attributes.AttributeModifier
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.EnumAction
import net.minecraft.item.ItemStack
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumHand
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import shadowfox.botanicaladdons.api.item.IWeightEnchantable
import shadowfox.botanicaladdons.common.enchantment.EnchantmentWeight
import shadowfox.botanicaladdons.common.items.base.IPreventBreakInCreative
import shadowfox.botanicaladdons.common.items.base.ItemMod
import vazkii.botania.api.mana.ManaItemHandler
import vazkii.botania.common.item.equipment.tool.ToolCommons

/**
 * @author WireSegal
 * Created at 9:20 PM on 5/18/16.
 */
class ItemThunderFists(name: String) : ItemMod(name), IWeightEnchantable, IPreventBreakInCreative {
    val MANA_PER_DAMAGE = 40

    init {
        setMaxStackSize(1)
        maxDamage = 1561
        addPropertyOverride(ResourceLocation("blocking")) {
            stack, worldIn, entityIn ->
            if (entityIn != null && entityIn.isHandActive && (entityIn.heldItemMainhand == stack || entityIn.heldItemOffhand == stack)) 1f else 0f
        }
    }

    private val attackDamage = 5.5f

    override fun getItemUseAction(stack: ItemStack?) = EnumAction.BLOCK

    override fun onUpdate(stack: ItemStack, world: World, player: Entity, itemSlot: Int, isSelected: Boolean) {
        if (!world.isRemote && player is EntityPlayer && stack.itemDamage > 0 && ManaItemHandler.requestManaExactForTool(stack, player, MANA_PER_DAMAGE * 2, true))
            stack.itemDamage = stack.itemDamage - 1
    }

    override fun onBlockDestroyed(stack: ItemStack, worldIn: World, blockIn: IBlockState, pos: BlockPos, entityLiving: EntityLivingBase): Boolean {
        if (blockIn.getBlockHardness(worldIn, pos) > 0)
            ToolCommons.damageItem(stack, 2, entityLiving, MANA_PER_DAMAGE)
        return true
    }

    override fun getMaxItemUseDuration(stack: ItemStack?): Int {
        return 72000
    }

    override fun onUsingTick(stack: ItemStack, player: EntityLivingBase, count: Int) {
        if (player.heldItemMainhand?.item != this)
            player.stopActiveHand()
    }

    override fun hitEntity(stack: ItemStack?, target: EntityLivingBase?, attacker: EntityLivingBase?): Boolean {
        ToolCommons.damageItem(stack, 1, attacker, MANA_PER_DAMAGE)
        return super.hitEntity(stack, target, attacker)
    }

    override fun onItemRightClick(itemStackIn: ItemStack, worldIn: World, playerIn: EntityPlayer, hand: EnumHand): ActionResult<ItemStack>? {
        if (hand == EnumHand.OFF_HAND && playerIn.heldItemMainhand?.item == this)
            playerIn.activeHand = hand
        return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand)
    }

    override fun getAttributeModifiers(slot: EntityEquipmentSlot?, stack: ItemStack): Multimap<String, AttributeModifier>? {
        val multimap = super.getAttributeModifiers(slot, stack)
        if (slot == EntityEquipmentSlot.MAINHAND) {
            val offset = EnchantmentWeight.getWeight(stack)
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.attributeUnlocalizedName, AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage.toDouble(), 0))
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.attributeUnlocalizedName, AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.0 + offset * -.3, 0))
        }
        return multimap
    }

    override fun getItemEnchantability(): Int = 14
    override fun getIsRepairable(stack: ItemStack?, repair: ItemStack): Boolean {
        return repair.item == ModItems.resource && (repair.itemDamage % ItemResource.Variants.values().size == ItemResource.Variants.THUNDER_STEEL.ordinal)
    }

    override fun canApplyWeightEnchantment(stack: ItemStack, ench: Enchantment) = true
}