using Microsoft.EntityFrameworkCore.Migrations;

namespace Wxsc.Migrations
{
    public partial class favfood : Migration
    {
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<int>(
                name: "MenuItemId",
                table: "FavFoods",
                nullable: false,
                defaultValue: 0);

            migrationBuilder.AddColumn<string>(
                name: "UserId",
                table: "FavFoods",
                nullable: true);
        }

        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "MenuItemId",
                table: "FavFoods");

            migrationBuilder.DropColumn(
                name: "UserId",
                table: "FavFoods");
        }
    }
}
